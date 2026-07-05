package com.winx.fleet.controller;

import com.winx.fleet.dto.StatusUpdate;
import com.winx.fleet.dto.VehicleResponse;
import com.winx.fleet.model.Vehicle;
import com.winx.fleet.model.VehicleStatus;
import com.winx.fleet.repository.VehicleRepository;
import com.winx.fleet.service.VehicleAvailabilityService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final VehicleAvailabilityService availabilityService;

    public VehicleController(VehicleRepository vehicleRepository,
                             VehicleAvailabilityService availabilityService) {
        this.vehicleRepository = vehicleRepository;
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public Vehicle addVehicle(@RequestBody Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    @GetMapping
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream().map(VehicleResponse::from).toList();
    }

    @GetMapping("/{id}")
    public VehicleResponse getVehicleById(@PathVariable Long id) {
        return vehicleRepository.findById(id)
                .map(VehicleResponse::from)
                .orElse(null);
    }

    @DeleteMapping("/{id}")
    public void deleteVehicle(@PathVariable Long id) {
        vehicleRepository.deleteById(id);
    }

    // Required by BC-03 FleetClient: GET /vehicles/search?lat=&lon=&radiusKm=&type=&maxPrice=
    @GetMapping("/search")
    public List<VehicleResponse> searchVehicles(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radiusKm,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return availabilityService.findAvailableNear(lat, lon, radiusKm).stream()
                .filter(v -> type == null || v.getType().name().equalsIgnoreCase(type))
                .filter(v -> maxPrice == null || v.getPricePerUnit().compareTo(maxPrice) <= 0)
                .map(VehicleResponse::from)
                .toList();
    }

    // Required by BC-03 FleetClient: PATCH /vehicles/{id}/status
    @PatchMapping("/{id}/status")
    public VehicleResponse updateStatus(@PathVariable Long id, @RequestBody StatusUpdate request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + id));
        vehicle.setStatus(VehicleStatus.valueOf(request.status()));
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }
}
