package com.thewinx.identityaccess.infrastructure.rating;

import com.thewinx.identityaccess.infrastructure.rating.dto.RatingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "bc05-rating", url = "${rating.service.url:http://localhost:8085}")
public interface RatingClient {

    @GetMapping("/api/ratings/vehicle/{vehicleId}")
    List<RatingDto> getVehicleRatings(@PathVariable("vehicleId") Long vehicleId);

    @GetMapping("/api/ratings/vehicle/{vehicleId}/average")
    Map<String, Double> getAverageScore(@PathVariable("vehicleId") Long vehicleId);
}
