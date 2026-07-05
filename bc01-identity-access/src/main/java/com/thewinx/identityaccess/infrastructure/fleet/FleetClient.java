package com.thewinx.identityaccess.infrastructure.fleet;

import com.thewinx.identityaccess.infrastructure.fleet.dto.VehicleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client for communicating with the bc02-fleet-management service.
 * All cross-context data flows through this REST client - never via a shared database.
 *
 * name must match spring.application.name in bc02-fleet-management/application.yml.
 * url is used as a direct fallback when Eureka is not running (standalone mode).
 */
@FeignClient(
        name = "bc02-fleet-management",
        url = "${fleet.service.url:http://localhost:8082}",
        fallback = FleetClientFallback.class
)
public interface FleetClient {

    /**
     * Retrieve all vehicles from bc02.
     * Matches: GET http://localhost:8082/vehicles
     */
    @GetMapping("/vehicles")
    List<VehicleDto> getAllVehicles();

    /**
     * Retrieve a single vehicle by its ID from bc02.
     * Matches: GET http://localhost:8082/vehicles/{id}
     */
    @GetMapping("/vehicles/{id}")
    VehicleDto getVehicleById(@PathVariable("id") Long id);
}
