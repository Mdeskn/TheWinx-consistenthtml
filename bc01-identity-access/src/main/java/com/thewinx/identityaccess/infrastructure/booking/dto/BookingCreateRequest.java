package com.thewinx.identityaccess.infrastructure.booking.dto;

public record BookingCreateRequest(Long vehicleId, Double startLatitude, Double startLongitude, String paymentMethod) {}
