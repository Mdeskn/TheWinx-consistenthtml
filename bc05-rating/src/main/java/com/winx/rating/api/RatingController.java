package com.winx.rating.api;

import com.winx.rating.api.dto.RatingResponse;
import com.winx.rating.api.dto.SubmitRatingRequest;
import com.winx.rating.application.RatingQueryService;
import com.winx.rating.application.RatingSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Submit and query ride ratings")
public class RatingController {

    private final RatingSubmissionService submissionService;
    private final RatingQueryService queryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a rating for a completed booking")
    public RatingResponse submit(@Valid @RequestBody SubmitRatingRequest req) {
        return RatingResponse.from(
                submissionService.submitRating(
                        req.bookingId(), req.userId(),
                        req.vehicleId(), req.providerId(),
                        req.vehicleScore(), req.providerScore(),
                        req.comment()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a rating by its ID")
    public RatingResponse getById(@PathVariable Long id) {
        return RatingResponse.from(queryService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List all ratings")
    public List<RatingResponse> getAll() {
        return queryService.getAll().stream().map(RatingResponse::from).toList();
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "List ratings for a vehicle")
    public List<RatingResponse> getByVehicle(@PathVariable Long vehicleId) {
        return queryService.getVehicleRatings(vehicleId).stream()
                .map(RatingResponse::from).toList();
    }

    @GetMapping("/vehicle/{vehicleId}/average")
    @Operation(summary = "Average vehicle score")
    public Map<String, Double> getAverageVehicleScore(@PathVariable Long vehicleId) {
        return Map.of("averageVehicleScore", queryService.getAverageVehicleScore(vehicleId));
    }

    @GetMapping("/provider/{providerId}")
    @Operation(summary = "List ratings for a provider")
    public List<RatingResponse> getByProvider(@PathVariable Long providerId) {
        return queryService.getProviderRatings(providerId).stream()
                .map(RatingResponse::from).toList();
    }

    @GetMapping("/provider/{providerId}/average")
    @Operation(summary = "Average provider score")
    public Map<String, Double> getAverageProviderScore(@PathVariable Long providerId) {
        return Map.of("averageProviderScore", queryService.getAverageProviderScore(providerId));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Check whether a booking has been rated")
    public Map<String, Object> getByBooking(@PathVariable Long bookingId) {
        Optional<RatingResponse> resp = queryService.getByBookingId(bookingId)
                .map(RatingResponse::from);
        if (resp.isPresent()) {
            return Map.of("rated", true, "rating", resp.get());
        }
        return Map.of("rated", false);
    }
}
