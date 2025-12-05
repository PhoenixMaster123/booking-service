package springboot.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import springboot.bookingservice.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {

  private UUID id;
  private UUID userId;
  private LocalDateTime bookingDate;
  private BookingStatus status;
  private List<UUID> serviceIds;
  private UUID vehicleId;
  private String additionalNotes;
  private String paymentMethod;
  private String phoneNumber;
  private BigDecimal totalPrice;
  private String vehicleDescription;
  private String serviceNames;
}