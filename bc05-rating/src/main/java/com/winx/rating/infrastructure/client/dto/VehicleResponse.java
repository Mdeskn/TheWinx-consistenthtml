package com.winx.rating.infrastructure.client.dto;

import java.math.BigDecimal;

public record VehicleResponse(
        Long vehicleId,
        Long providerId,
        String name,
        String type,
        String description,
        String status,
        BigDecimal pricePerUnit,
        String billingModel,
        Integer minAge,
        Integer maxPersons
) {}
