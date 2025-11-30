package springboot.bookingservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springboot.bookingservice.dto.BookingRequest;
import springboot.bookingservice.dto.BookingResponse;
import springboot.bookingservice.dto.GetBookingResponse;
import springboot.bookingservice.model.Booking;
import springboot.bookingservice.model.BookingStatus;
import springboot.bookingservice.repository.BookingRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public void createBooking(BookingRequest request) {

        BookingStatus statusToSave = (request.getStatus() != null)
                ? BookingStatus.valueOf(request.getStatus())
                : BookingStatus.PENDING;

        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .bookingDate(request.getBookingDate())
                .status(statusToSave)
                .additionalNotes(request.getAdditionalNotes())
                .totalPrice(request.getTotalPrice())
                .vehicleId(request.getVehicleId())
                .build();

        bookingRepository.save(booking);

        log.info("Booking created: {}", booking);
    }

    public ResponseEntity<GetBookingResponse> getBookingsByUser(UUID userId) {

        List<Booking> entities = bookingRepository.findByUserId(userId);

        List<BookingResponse> dtos = entities.stream()
                .map(booking -> BookingResponse.builder()
                        .id(booking.getId())
                        .userId(booking.getUserId())
                        .bookingDate(booking.getBookingDate())
                        .status(booking.getStatus())
                        .additionalNotes(booking.getAdditionalNotes())
                        .totalPrice(booking.getTotalPrice())
                        .vehicleId(booking.getVehicleId())
                        .build())
                .toList();

        GetBookingResponse response = GetBookingResponse.builder()
                .bookings(dtos)
                .build();

        return ResponseEntity.ok(response);
    }
}