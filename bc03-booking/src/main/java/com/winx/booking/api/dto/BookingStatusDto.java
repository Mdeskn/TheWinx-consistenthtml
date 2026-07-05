package com.winx.booking.api.dto;

import com.winx.booking.domain.Booking;

/**
 * Lightweight status view. Also, the contract Rating (downstream, Conformist) reads to verify a
 * booking is COMPLETED before allowing a rating.
 */
public record BookingStatusDto(Long bookingId, String status) {

    public static BookingStatusDto from(Booking b) {
        return new BookingStatusDto(b.getBookingId(), b.getStatus().name());
    }
}
