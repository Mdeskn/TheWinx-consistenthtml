package com.thewinx.identityaccess.api;

import com.thewinx.identityaccess.infrastructure.fleet.FleetClient;
import com.thewinx.identityaccess.infrastructure.fleet.dto.VehicleDto;
import com.thewinx.identityaccess.infrastructure.rating.RatingClient;
import com.thewinx.identityaccess.infrastructure.rating.dto.RatingDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleDetailController {

    private final FleetClient fleetClient;
    private final RatingClient ratingClient;

    public VehicleDetailController(FleetClient fleetClient, RatingClient ratingClient) {
        this.fleetClient = fleetClient;
        this.ratingClient = ratingClient;
    }

    @GetMapping("/{id}/detail")
    public Map<String, Object> vehicleDetail(@PathVariable Long id) {
        VehicleDto vehicle;
        try {
            vehicle = fleetClient.getVehicleById(id);
        } catch (Exception e) {
            vehicle = null;
        }

        List<RatingDto> ratings = List.of();
        double avgScore = 0.0;
        try {
            ratings  = ratingClient.getVehicleRatings(id);
            avgScore = ratingClient.getAverageScore(id)
                    .getOrDefault("averageVehicleScore", 0.0);
        } catch (Exception ignored) {}

        return Map.of(
                "vehicle",      vehicle != null ? vehicle : Map.of(),
                "ratings",      ratings,
                "averageScore", avgScore
        );
    }
}
