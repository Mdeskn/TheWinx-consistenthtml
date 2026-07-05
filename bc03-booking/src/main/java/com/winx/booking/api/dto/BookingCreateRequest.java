package com.winx.booking.api.dto;

import jakarta.validation.constraints.NotNull;

public record BookingCreateRequest(
        @NotNull Long vehicleId,
        @NotNull Double startLatitude,
        @NotNull Double startLongitude,
        String paymentMethod
) {
}
