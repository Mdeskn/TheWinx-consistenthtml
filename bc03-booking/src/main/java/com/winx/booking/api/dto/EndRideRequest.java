package com.winx.booking.api.dto;

import jakarta.validation.constraints.NotNull;

/** Request to end an active ride. */
public record EndRideRequest(
        @NotNull Double endLatitude,
        @NotNull Double endLongitude,
        @NotNull Double distanceKm
) {
}
