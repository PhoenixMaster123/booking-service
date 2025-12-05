package springboot.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import springboot.bookingservice.dto.BookingRequest;
import springboot.bookingservice.dto.BookingResponse;
import springboot.bookingservice.dto.GetBookingResponse;
import springboot.bookingservice.model.BookingStatus;
import springboot.bookingservice.service.BookingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @BeforeEach
    void setup() {
        Mockito.reset(bookingService);
    }

    @TestConfiguration
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        BookingService bookingService() { return org.mockito.Mockito.mock(BookingService.class); }
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        SimpMessagingTemplate simpMessagingTemplate() { return org.mockito.Mockito.mock(SimpMessagingTemplate.class); }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private BookingRequest validRequest() {
        return BookingRequest.builder()
                .userId(UUID.randomUUID())
                .bookingDate(LocalDateTime.now().plusDays(2))
                .serviceIds(List.of(UUID.randomUUID()))
                .vehicleId(UUID.randomUUID())
                .additionalNotes("Test note")
                .paymentMethod("CARD")
                .phoneNumber("+123456789")
                .totalPrice(new BigDecimal("15.50"))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/bookings returns 200 OK for valid request")
    void createBooking_valid() throws Exception {
        BookingRequest req = validRequest();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).createBooking(any(BookingRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/bookings returns 400 for invalid request (missing fields)")
    void createBooking_invalid() throws Exception {
        BookingRequest invalid = BookingRequest.builder().build();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @Test
    @DisplayName("GET /api/v1/bookings?userId=... delegates to service and returns payload")
    void getBookings_byUser() throws Exception {
        UUID uid = UUID.randomUUID();
        BookingResponse br = BookingResponse.builder()
                .id(UUID.randomUUID())
                .userId(uid)
                .status(BookingStatus.PENDING)
                .vehicleDescription("Vehicle abc")
                .build();
        GetBookingResponse payload = GetBookingResponse.builder().bookings(List.of(br)).build();
        Mockito.when(bookingService.getBookingsByUser(uid)).thenReturn(ResponseEntity.ok(payload));

        mockMvc.perform(get("/api/v1/bookings").param("userId", uid.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookings", hasSize(1)))
                .andExpect(jsonPath("$.bookings[0].userId", is(uid.toString())));

        verify(bookingService, times(1)).getBookingsByUser(eq(uid));
    }

    @Test
    @DisplayName("GET /api/v1/bookings?status=... delegates to service and returns payload")
    void getBookings_byStatus() throws Exception {
        String status = "confirmed";
        BookingResponse br = BookingResponse.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(BookingStatus.CONFIRMED)
                .vehicleDescription("Vehicle abc")
                .build();
        GetBookingResponse payload = GetBookingResponse.builder().bookings(List.of(br)).build();
        Mockito.when(bookingService.getBookingsByStatus(status)).thenReturn(ResponseEntity.ok(payload));

        mockMvc.perform(get("/api/v1/bookings").param("status", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookings", hasSize(1)))
                .andExpect(jsonPath("$.bookings[0].status", is(BookingStatus.CONFIRMED.name())));

        verify(bookingService, times(1)).getBookingsByStatus(eq(status));
    }

    @Test
    @DisplayName("GET /api/v1/bookings?status=invalid returns 400 when service rejects")
    void getBookings_byStatus_invalid() throws Exception {
        String status = "invalid";
        Mockito.when(bookingService.getBookingsByStatus(status)).thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(get("/api/v1/bookings").param("status", status))
                .andExpect(status().isBadRequest());

        verify(bookingService, times(1)).getBookingsByStatus(eq(status));
    }

    @Test
    @DisplayName("GET /api/v1/bookings with both userId and status prefers userId branch")
    void getBookings_bothParams_prefersUserId() throws Exception {
        UUID uid = UUID.randomUUID();
        GetBookingResponse payload = GetBookingResponse.builder().bookings(List.of()).build();
        Mockito.when(bookingService.getBookingsByUser(uid)).thenReturn(ResponseEntity.ok(payload));

        mockMvc.perform(get("/api/v1/bookings").param("userId", uid.toString()).param("status", "pending"))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getBookingsByUser(eq(uid));
        Mockito.verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("GET /api/v1/bookings without params returns 400")
    void getBookings_missingParams() throws Exception {
        mockMvc.perform(get("/api/v1/bookings"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/bookings/{id}/cancel calls service and returns 200")
    void cancelBooking() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/bookings/" + id + "/cancel"))
                .andExpect(status().isOk());
        verify(bookingService, times(1)).cancelBooking(eq(id));
    }

    @Test
    @DisplayName("POST /api/v1/bookings/{id}/archive calls service and returns 200")
    void archiveBooking() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/bookings/" + id + "/archive"))
                .andExpect(status().isOk());
        verify(bookingService, times(1)).archiveBooking(eq(id));
    }
}
