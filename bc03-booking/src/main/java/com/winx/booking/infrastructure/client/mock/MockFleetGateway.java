package com.winx.booking.infrastructure.client.mock;

import com.winx.booking.api.dto.VehicleDto;
import com.winx.booking.infrastructure.client.FleetGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("mock")
public class MockFleetGateway implements FleetGateway {

    private VehicleDto sample(Long id, String status) {
        return new VehicleDto(id, null, "E_SCOOTER", "Sample e-scooter " + id, status,
                new BigDecimal("0.20"), "PER_HOUR", 18);
    }

    @Override
    public VehicleDto findVehicle(Long id) {
        return sample(id, "AVAILABLE");
    }

    @Override
    public List<VehicleDto> search(double lat, double lon, double radiusKm, String type, BigDecimal maxPrice) {
        return List.of(sample(1L, "AVAILABLE"), sample(2L, "AVAILABLE"));
    }

    @Override
    public void markBooked(Long vehicleId) {
    }

    @Override
    public void markAvailable(Long vehicleId) {
    }
}
