package springboot.bookingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springboot.bookingservice.model.Booking;
import springboot.bookingservice.model.BookingStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUserId(UUID userId);

    List<Booking> findByStatus(BookingStatus status);
}