package springboot.bookingservice.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
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

//    messagingTemplate.convertAndSend("/topic/bookings", booking);

    return ResponseEntity.ok().build();
  }

    @GetMapping
    public ResponseEntity<GetBookingResponse> getAllBookings(@RequestParam("userId") UUID userId){
        return bookingService.getBookingsByUser(userId);
    }
}