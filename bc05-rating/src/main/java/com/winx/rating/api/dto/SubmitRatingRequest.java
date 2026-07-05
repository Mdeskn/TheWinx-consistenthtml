package com.winx.rating.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubmitRatingRequest(
        @NotNull(message = "bookingId is required")
        Long bookingId,

        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "vehicleId is required")
        Long vehicleId,

        @NotNull(message = "providerId is required")
        Long providerId,

        @NotNull @Min(1) @Max(5)
        Integer vehicleScore,

        @NotNull @Min(1) @Max(5)
        Integer providerScore,

        String comment
) {}
