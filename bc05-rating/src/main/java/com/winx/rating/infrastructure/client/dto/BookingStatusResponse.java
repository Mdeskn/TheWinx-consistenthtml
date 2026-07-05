package com.winx.rating.infrastructure.client.dto;

/** Minimal projection of a Booking returned by bc03-booking. */
public record BookingStatusResponse(
        Long bookingId,
        Long userId,
        String status   // ACTIVE | COMPLETED | CANCELLED
) {}
