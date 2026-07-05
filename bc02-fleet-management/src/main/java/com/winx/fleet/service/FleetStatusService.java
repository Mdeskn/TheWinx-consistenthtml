package com.winx.fleet.service;

import com.winx.fleet.dto.UpdateLocationRequest;
import com.winx.fleet.dto.UpdateStatusRequest;
import com.winx.fleet.model.Vehicle;
import com.winx.fleet.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FleetStatusService {

    private final VehicleRepository vehicleRepository;

    public FleetStatusService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> getFleet(Long providerId) {
        return vehicleRepository.findByProviderId(providerId);
    }

    public Vehicle updateLocation(Long id, UpdateLocationRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicle.setCurrentLatitude(request.latitude);
        vehicle.setCurrentLongitude(request.longitude);

        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateStatus(Long id, UpdateStatusRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicle.setStatus(request.status);

        return vehicleRepository.save(vehicle);
    }
}
