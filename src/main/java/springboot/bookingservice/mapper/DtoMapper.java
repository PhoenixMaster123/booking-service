package springboot.bookingservice.mapper;

import lombok.experimental.UtilityClass;
import springboot.bookingservice.dto.BookingResponse;
import springboot.bookingservice.model.Booking;

@UtilityClass
public class DtoMapper {

    public static BookingResponse mapToResponse(Booking booking, String vehicleDesc, String serviceNamesList) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .bookingDate(booking.getBookingDate())
                .status(booking.getStatus())
                .serviceIds(booking.getServiceIds())
                .vehicleId(booking.getVehicleId())
                .additionalNotes(booking.getAdditionalNotes())
                .paymentMethod(booking.getPaymentMethod())
                .phoneNumber(booking.getPhoneNumber())
                .totalPrice(booking.getTotalPrice())
                .vehicleDescription(vehicleDesc)
                .serviceNames(serviceNamesList)
                .build();
    }
}
