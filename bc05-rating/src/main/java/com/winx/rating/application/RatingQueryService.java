package com.winx.rating.application;

import com.winx.rating.domain.Rating;
import com.winx.rating.infrastructure.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RatingQueryService {

    private final RatingRepository ratingRepository;

    public Rating getById(Long id) {
        return ratingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rating not found: " + id));
    }

    public List<Rating> getAll() {
        return ratingRepository.findAll();
    }

    public List<Rating> getVehicleRatings(Long vehicleId) {
        return ratingRepository.findByTarget_VehicleId(vehicleId);
    }

    public List<Rating> getProviderRatings(Long providerId) {
        return ratingRepository.findByTarget_ProviderId(providerId);
    }

    public Optional<Rating> getByBookingId(Long bookingId) {
        return ratingRepository.findByTarget_BookingId(bookingId);
    }

    public double getAverageVehicleScore(Long vehicleId) {
        return ratingRepository.findByTarget_VehicleId(vehicleId).stream()
                .mapToInt(r -> r.getReview().getVehicleScore().getValue())
                .average()
                .orElse(0.0);
    }
}
