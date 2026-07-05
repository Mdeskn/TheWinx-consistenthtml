package com.winx.rating.infrastructure.client.dto;

/** Minimal projection of a Vehicle returned by bc02-fleet-management. */
public record VehicleResponse(
        Long vehicleId,
        Long providerId,
        String type,          // E_SCOOTER | BICYCLE | E_BIKE | E_CAR
        String description,
        String status         // AVAILABLE | BOOKED
) {}
