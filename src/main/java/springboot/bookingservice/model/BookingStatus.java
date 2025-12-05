package springboot.bookingservice.model;

/**
 * Enum for Booking Status.
 *
 * @author Kristian Popov
 */
public enum BookingStatus {
  PENDING,
  CONFIRMED,
  CANCELLED,
  COMPLETED,
  ARCHIVED
}