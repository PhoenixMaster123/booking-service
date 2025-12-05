package springboot.bookingservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import springboot.bookingservice.dto.BookingRequest;
import springboot.bookingservice.dto.GetBookingResponse;
import springboot.bookingservice.model.Booking;
import springboot.bookingservice.model.BookingStatus;
import springboot.bookingservice.repository.BookingRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    private BookingRequest request;
    private UUID userId;
    private UUID vehicleId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
        request = BookingRequest.builder()
                .userId(userId)
                .bookingDate(LocalDateTime.now().plusDays(1))
                .serviceIds(List.of(UUID.randomUUID(), UUID.randomUUID()))
                .vehicleId(vehicleId)
                .additionalNotes("Please be careful")
                .paymentMethod("CARD")
                .phoneNumber("+123456789")
                .totalPrice(new BigDecimal("99.99"))
                .build();
    }

    @Test
    @DisplayName("createBooking saves a booking with PENDING status")
    void createBooking_savesWithPending() {

        bookingService.createBooking(request);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(1)).save(captor.capture());
        Booking saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getVehicleId()).isEqualTo(vehicleId);
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(saved.getServiceIds()).hasSize(2);
        assertThat(saved.getTotalPrice()).isEqualByComparingTo("99.99");
    }

    @Test
    @DisplayName("getBookingsByUser returns empty list response when no bookings")
    void getBookingsByUser_empty() {
        UUID uid = UUID.randomUUID();
        when(bookingRepository.findByUserId(uid)).thenReturn(Collections.emptyList());

        ResponseEntity<GetBookingResponse> response = bookingService.getBookingsByUser(uid);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBookings()).isEmpty();
    }

    @Test
    @DisplayName("getBookingsByUser maps and enriches non-empty list")
    void getBookingsByUser_nonEmpty() {
        UUID uid = UUID.randomUUID();
        Booking b = Booking.builder()
                .id(UUID.randomUUID())
                .userId(uid)
                .vehicleId(UUID.randomUUID())
                .bookingDate(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .serviceIds(List.of(UUID.randomUUID()))
                .totalPrice(new BigDecimal("10.00"))
                .build();
        when(bookingRepository.findByUserId(uid)).thenReturn(List.of(b));

        ResponseEntity<GetBookingResponse> response = bookingService.getBookingsByUser(uid);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBookings()).hasSize(1);
        assertThat(response.getBody().getBookings().get(0).getVehicleDescription()).startsWith("Vehicle ");
    }

    @Test
    @DisplayName("getBookingsByStatus returns 400 for invalid status")
    void getBookingsByStatus_invalid() {
        ResponseEntity<GetBookingResponse> response = bookingService.getBookingsByStatus("nope");
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @DisplayName("getBookingsByStatus returns enriched DTOs for valid status")
    void getBookingsByStatus_valid() {
        Booking b = Booking.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .vehicleId(UUID.randomUUID())
                .bookingDate(LocalDateTime.now())
                .status(BookingStatus.CANCELLED)
                .serviceIds(List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .totalPrice(new BigDecimal("30.00"))
                .build();
        when(bookingRepository.findByStatus(BookingStatus.CANCELLED)).thenReturn(List.of(b));

        ResponseEntity<GetBookingResponse> response = bookingService.getBookingsByStatus("cancelled");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBookings()).hasSize(1);
        assertThat(response.getBody().getBookings().get(0).getServiceNames()).contains("Service(s) Selected");
    }

    @Test
    @DisplayName("cancelBooking sets status to CANCELLED when exists")
    void cancelBooking_found() {
        UUID id = UUID.randomUUID();
        Booking existing = Booking.builder().id(id).status(BookingStatus.CONFIRMED).build();
        when(bookingRepository.findById(id)).thenReturn(Optional.of(existing));

        bookingService.cancelBooking(id);

        assertThat(existing.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(existing);
    }

    @Test
    @DisplayName("cancelBooking throws when not found")
    void cancelBooking_notFound() {
        UUID id = UUID.randomUUID();
        when(bookingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    @DisplayName("archiveBooking sets status to ARCHIVED when exists")
    void archiveBooking_found() {
        UUID id = UUID.randomUUID();
        Booking existing = Booking.builder().id(id).status(BookingStatus.COMPLETED).build();
        when(bookingRepository.findById(id)).thenReturn(Optional.of(existing));

        bookingService.archiveBooking(id);

        assertThat(existing.getStatus()).isEqualTo(BookingStatus.ARCHIVED);
        verify(bookingRepository).save(existing);
    }

    @Test
    @DisplayName("archiveBooking throws when not found")
    void archiveBooking_notFound() {
        UUID id = UUID.randomUUID();
        when(bookingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.archiveBooking(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Booking not found");
    }
}
