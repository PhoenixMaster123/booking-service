package springboot.bookingservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springboot.bookingservice.dto.BookingRequest;
import springboot.bookingservice.dto.BookingResponse;
import springboot.bookingservice.dto.GetBookingResponse;
import springboot.bookingservice.mapper.DtoMapper;
import springboot.bookingservice.model.Booking;
import springboot.bookingservice.model.BookingStatus;
import springboot.bookingservice.repository.BookingRepository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Create a new booking.
     */
    @Transactional
    public void createBooking(BookingRequest request) {

        BookingStatus statusToSave = BookingStatus.PENDING;

        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .bookingDate(request.getBookingDate())
                .status(statusToSave)
                .additionalNotes(request.getAdditionalNotes())
                .totalPrice(request.getTotalPrice())
                .vehicleId(request.getVehicleId())
                .serviceIds(request.getServiceIds())
                .paymentMethod(request.getPaymentMethod())
                .phoneNumber(request.getPhoneNumber())
                .build();

        bookingRepository.save(booking);
        log.info("Booking created with ID: {}", booking.getId());
    }

    /**
     * Get bookings by User ID (Enriched with names).
     */
    public ResponseEntity<GetBookingResponse> getBookingsByUser(UUID userId) {
        List<Booking> entities = bookingRepository.findByUserId(userId);

        if (entities == null || entities.isEmpty()) {
            return ResponseEntity.ok(GetBookingResponse.builder()
                    .bookings(Collections.emptyList())
                    .build());
        }

        List<BookingResponse> dtos = entities.stream()
                .map(this::enrichAndMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(GetBookingResponse.builder().bookings(dtos).build());
    }

    /**
     * Get bookings by Status (Enriched with names).
     */
    public ResponseEntity<GetBookingResponse> getBookingsByStatus(String status) {
        BookingStatus statusEnum;
        try {
            statusEnum = BookingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid status requested: {}", status);
            return ResponseEntity.badRequest().build();
        }

        List<Booking> bookings = bookingRepository.findByStatus(statusEnum);

        List<BookingResponse> dtos = bookings.stream()
                .map(this::enrichAndMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new GetBookingResponse(dtos));
    }

    @Transactional
    public void cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional
    public void archiveBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        // Assuming ARCHIVED exists in your Enum, otherwise use boolean flag
        booking.setStatus(BookingStatus.ARCHIVED);
        bookingRepository.save(booking);
    }

    /**
     * Orchestrates the mapping and fetching of external data.
     */
    private BookingResponse enrichAndMap(Booking booking) {
        String vehicleName = getVehicleNameFromExternalService(booking.getVehicleId());

        String serviceNames = getServiceNamesFromExternalService(booking.getServiceIds());

        return DtoMapper.mapToResponse(booking, vehicleName, serviceNames);
    }

    private String getVehicleNameFromExternalService(UUID vehicleId) {
        if (vehicleId == null) return "Unknown Vehicle";
        return "Vehicle " + vehicleId.toString().substring(0, 5) + "...";
    }

    private String getServiceNamesFromExternalService(List<UUID> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) return "No Services";
        return serviceIds.size() + " Service(s) Selected";
    }
}