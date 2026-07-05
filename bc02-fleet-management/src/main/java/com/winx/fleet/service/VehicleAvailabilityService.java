package com.winx.fleet.service;

import com.winx.fleet.model.Vehicle;
import com.winx.fleet.repository.VehicleRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class VehicleAvailabilityService {

    private final VehicleRepository vehicleRepository;

    public VehicleAvailabilityService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // GET ALL VEHICLES
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    // FIND BY ID
    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }

    // SEARCH AVAILABLE VEHICLES NEAR LOCATION
    @CircuitBreaker(name = "vehicleDb", fallbackMethod = "findAvailableNearFallback")
    public List<Vehicle> findAvailableNear(Double lat, Double lon, Double radiusKm) {
        return vehicleRepository.findAvailableNear(lat, lon, radiusKm);
    }

    public List<Vehicle> findAvailableNearFallback(Double lat, Double lon, Double radiusKm, Throwable t) {
        return Collections.emptyList();
    }
}
