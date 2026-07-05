package com.winx.booking.infrastructure.client;

import com.winx.booking.api.dto.VehicleDto;

import java.math.BigDecimal;
import java.util.List;

public interface FleetGateway {

    VehicleDto findVehicle(Long id);

    List<VehicleDto> search(double lat, double lon, double radiusKm, String type, BigDecimal maxPrice);

    void markBooked(Long vehicleId);

    void markAvailable(Long vehicleId);
}
