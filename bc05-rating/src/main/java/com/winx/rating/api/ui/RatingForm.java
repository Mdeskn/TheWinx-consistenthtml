package com.winx.rating.api.ui;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Mutable form bean for Thymeleaf binding (records lack setters). */
@Data
public class RatingForm {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotNull @Min(1) @Max(5)
    private Integer vehicleScore;

    @NotNull @Min(1) @Max(5)
    private Integer providerScore;

    private String comment;
}
