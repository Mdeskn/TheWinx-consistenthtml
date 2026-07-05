package com.thewinx.identityaccess.infrastructure.fleet;

import com.thewinx.identityaccess.infrastructure.fleet.dto.VehicleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fallback implementation for FleetClient.
 * This class is invoked automatically by the Resilience4j circuit breaker
 * when the bc02-fleet-management service is unavailable, too slow, or returning errors.
 *
 * Instead of crashing the identity-access service, the fallback returns
 * safe default values so the dashboard remains partially functional.
 */
@Component
public class FleetClientFallback implements FleetClient {

    private static final Logger log = LoggerFactory.getLogger(FleetClientFallback.class);

    /**
     * Returns an empty list when the fleet service is unreachable.
     * The dashboard will show "No vehicles available" instead of an error.
     */
    @Override
    public List<VehicleDto> getAllVehicles() {
        log.warn("FleetClient fallback triggered: fleet-service is unavailable. Returning empty vehicle list.");
        return Collections.emptyList();
    }

    /**
     * Returns null when a specific vehicle cannot be fetched.
     * Callers should null-check the result before using it.
     */
    @Override
    public VehicleDto getVehicleById(Long id) {
        log.warn("FleetClient fallback triggered: cannot fetch vehicle id={}. Returning null.", id);
        return null;
    }
}
