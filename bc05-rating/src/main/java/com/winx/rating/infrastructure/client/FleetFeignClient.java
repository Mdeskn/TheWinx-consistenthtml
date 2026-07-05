package com.winx.rating.infrastructure.client;

import com.winx.rating.infrastructure.client.dto.VehicleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bc02-fleet-management", fallback = FleetFeignFallback.class)
public interface FleetFeignClient {

    @GetMapping("/vehicles/{id}")
    VehicleResponse getVehicle(@PathVariable("id") Long vehicleId);
}
