package com.winx.booking.api.dto;

import com.winx.booking.domain.Booking;
import com.winx.booking.domain.vo.RideLocation;
import com.winx.booking.domain.vo.RideSummary;
import com.winx.booking.domain.vo.TimeInterval;
import com.winx.booking.domain.vo.VehicleSnapshot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingDto(
        Long bookingId,
        Long userId,
        Long vehicleId,
        String vehicleType,
        BigDecimal pricePerUnit,
        String billingModel,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Double startLatitude,
        Double startLongitude,
        Double endLatitude,
        Double endLongitude,
        Double distanceKm,
        BigDecimal totalCost,
        String status
) {

    public static BookingDto from(Booking b) {
        VehicleSnapshot v = b.getVehicleSnapshot();
        TimeInterval i = b.getInterval();
        RideLocation start = b.getStartLocation();
        RideLocation end = b.getEndLocation();
        RideSummary s = b.getSummary();
        return new BookingDto(
                b.getBookingId(),
                b.getUserId(),
                v != null ? v.getVehicleId() : null,
                v != null ? v.getType() : null,
                v != null ? v.getPricePerUnit() : null,
                v != null ? v.getBillingModel() : null,
                i != null ? i.getStartTime() : null,
                i != null ? i.getEndTime() : null,
                start != null ? start.getLatitude() : null,
                start != null ? start.getLongitude() : null,
                end != null ? end.getLatitude() : null,
                end != null ? end.getLongitude() : null,
                s != null ? s.getDistanceKm() : null,
                s != null ? s.getTotalCost() : null,
                b.getStatus().name()
        );
    }
}
