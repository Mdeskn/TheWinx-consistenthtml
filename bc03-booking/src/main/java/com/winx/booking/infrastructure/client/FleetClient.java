package com.winx.booking.infrastructure.client;

import com.winx.booking.api.dto.StatusUpdate;
import com.winx.booking.api.dto.VehicleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "bc02-fleet-management", path = "/vehicles")
public interface FleetClient {

    @GetMapping("/{id}")
    VehicleDto findById(@PathVariable("id") Long id);

    @GetMapping("/search")
    List<VehicleDto> search(@RequestParam("lat") double lat,
                            @RequestParam("lon") double lon,
                            @RequestParam("radiusKm") double radiusKm,
                            @RequestParam(value = "type", required = false) String type,
                            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice);

    @PatchMapping("/{id}/status")
    VehicleDto updateStatus(@PathVariable("id") Long id, @RequestBody StatusUpdate update);
}
