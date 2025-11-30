package springboot.bookingservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private String additionalNotes;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    private LocalDateTime estimatedCompletionTime;

    private LocalDateTime actualCompletionTime;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "vehicle_id")
    private UUID vehicleId;

}