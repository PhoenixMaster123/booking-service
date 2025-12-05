package springboot.bookingservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Booking date is required")
    @Future(message = "Booking date must be in the future")
    private LocalDateTime bookingDate;

    @NotNull(message = "At least one service must be selected")
    private List<UUID> serviceIds;

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    private String additionalNotes;

    private String paymentMethod;

    private String phoneNumber;

    private BigDecimal totalPrice;
}