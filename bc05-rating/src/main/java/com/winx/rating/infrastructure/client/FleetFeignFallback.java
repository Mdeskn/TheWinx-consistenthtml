package com.winx.rating.infrastructure.client;

import com.winx.rating.infrastructure.client.dto.VehicleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Called by Resilience4j when the bc02-fleet-management circuit is open or the call fails.
 * Returns a placeholder vehicle so rating queries degrade gracefully.
 */
@Slf4j
@Component
public class FleetFeignFallback implements FleetFeignClient {

    @Override
    public VehicleResponse getVehicle(Long vehicleId) {
        log.warn("bc02-fleet-management unreachable - circuit open. Returning placeholder for vehicle {}.", vehicleId);
        return new VehicleResponse(vehicleId, null, "UNKNOWN", "Vehicle info unavailable", "UNKNOWN");
    }
}
