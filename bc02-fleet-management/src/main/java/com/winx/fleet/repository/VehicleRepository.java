package com.winx.fleet.repository;

import com.winx.fleet.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByProviderId(Long providerId);

    boolean existsByProviderIdAndNameIgnoreCase(Long providerId, String name);

    @Query("""
            SELECT v FROM Vehicle v
            WHERE
                v.status = 'AVAILABLE'
                AND
                (6371 * acos(
                    cos(radians(:lat)) * cos(radians(v.currentLatitude)) *
                    cos(radians(v.currentLongitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(v.currentLatitude))
                )) <= :radius
            """)
    List<Vehicle> findAvailableNear(Double lat, Double lon, Double radius);
}