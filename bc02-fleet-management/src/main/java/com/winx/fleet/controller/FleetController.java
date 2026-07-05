package com.winx.fleet.controller;

import com.winx.fleet.dto.CreateVehicleRequest;
import com.winx.fleet.dto.UpdateLocationRequest;
import com.winx.fleet.dto.UpdateStatusRequest;
import com.winx.fleet.dto.UpdateVehicleRequest;
import com.winx.fleet.model.Vehicle;
import com.winx.fleet.service.FleetStatusService;
import com.winx.fleet.service.VehicleAvailabilityService;
import com.winx.fleet.service.VehicleRegistrationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/providers")
@CrossOrigin(origins = "*")
public class FleetController {

    private final VehicleRegistrationService registrationService;
    private final FleetStatusService statusService;
    private final VehicleAvailabilityService availabilityService;

    public FleetController(VehicleRegistrationService registrationService,
            FleetStatusService statusService,
            VehicleAvailabilityService availabilityService) {
        this.registrationService = registrationService;
        this.statusService = statusService;
        this.availabilityService = availabilityService;
    }

    // CREATE VEHICLE
    @PostMapping("/{providerId}/vehicles")
    public Vehicle createVehicle(@PathVariable Long providerId,
            @RequestBody CreateVehicleRequest request) {
        request.providerId = providerId;
        return registrationService.createVehicle(request);
    }

    // GET ALL VEHICLES FOR PROVIDER
    @GetMapping("/{providerId}/vehicles")
    public List<Vehicle> getVehicles(@PathVariable Long providerId) {
        return statusService.getFleet(providerId);
    }

    // UPDATE VEHICLE DETAILS
    @PutMapping("/vehicles/{id}")
    public Vehicle updateVehicle(@PathVariable Long id,
            @RequestBody UpdateVehicleRequest request) {
        return registrationService.updateVehicle(id, request);
    }

    // UPDATE LOCATION
    @PatchMapping("/vehicles/{id}/location")
    public Vehicle updateLocation(@PathVariable Long id,
            @RequestBody UpdateLocationRequest request) {
        return statusService.updateLocation(id, request);
    }

    // UPDATE STATUS
    @PatchMapping("/vehicles/{id}/status")
    public Vehicle updateStatus(@PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        return statusService.updateStatus(id, request);
    }

    // FIND AVAILABLE NEAR
    @GetMapping("/vehicles/near")
    public List<Vehicle> findNear(@RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam Double radius) {
        return availabilityService.findAvailableNear(lat, lon, radius);
    }

    // DELETE VEHICLE
    @DeleteMapping("/vehicles/{id}")
    public void deleteVehicle(@PathVariable Long id) {
        registrationService.deleteVehicle(id);
    }
}