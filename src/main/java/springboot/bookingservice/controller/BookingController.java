package springboot.bookingservice.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springboot.bookingservice.dto.BookingRequest;
import springboot.bookingservice.dto.GetBookingResponse;
import springboot.bookingservice.service.BookingService;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/bookings")
public class BookingController {

  private final BookingService bookingService;
  private final SimpMessagingTemplate messagingTemplate;

  @Autowired
  public BookingController(BookingService bookingService, SimpMessagingTemplate messagingTemplate) {
    this.bookingService = bookingService;
    this.messagingTemplate = messagingTemplate;
  }

  @PostMapping
  public ResponseEntity<Void> createBooking(@Valid @RequestBody BookingRequest request) {

    bookingService.createBooking(request);

    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<GetBookingResponse> getBookings(
      @RequestParam(value = "userId", required = false) UUID userId,
      @RequestParam(value = "status", required = false) String status) {

    if (userId != null) {
      return bookingService.getBookingsByUser(userId);
    } else if (status != null) {
      return bookingService.getBookingsByStatus(status);
    } else {
      return ResponseEntity.badRequest().build();
    }
  }

    /**
     * Endpoint to cancel a booking.
     * Called by Scheduler when time expires.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable("id") UUID bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint to archive a booking.
     * Called by Scheduler for old data.
     */
    @PostMapping("/{id}/archive")
    public ResponseEntity<Void> archiveBooking(@PathVariable("id") UUID bookingId) {
        bookingService.archiveBooking(bookingId);
        return ResponseEntity.ok().build();
    }
}