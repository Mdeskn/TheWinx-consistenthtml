package com.winx.fleet.dto;

import com.winx.fleet.model.Vehicle;

import java.math.BigDecimal;

public record VehicleResponse(
        Long vehicleId,
        String name,
        String type,
        String description,
        String status,
        BigDecimal pricePerUnit,
        String billingModel,
        Integer minAge,
        Integer maxPersons,
        Double currentLatitude,
        Double currentLongitude
) {
    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(
                v.getId(),
                v.getName(),
                v.getType() != null ? v.getType().name() : null,
                v.getDescription(),
                v.getStatus() != null ? v.getStatus().name() : null,
                v.getPricePerUnit(),
                v.getBillingModel() != null ? v.getBillingModel().name() : null,
                v.getMinAge(),
                v.getMaxPersons(),
                v.getCurrentLatitude(),
                v.getCurrentLongitude()
        );
    }
}
