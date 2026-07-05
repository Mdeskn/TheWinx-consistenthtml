package com.winx.booking.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VehicleDto(
        Long vehicleId,
        Long providerId,
        String type,
        String description,
        String status,
        BigDecimal pricePerUnit,
        String billingModel,
        Integer minAge
) {
}
