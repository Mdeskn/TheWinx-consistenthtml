package com.winx.rating.infrastructure;

import com.winx.rating.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating>     findByTarget_VehicleId(Long vehicleId);
    List<Rating>     findByTarget_ProviderId(Long providerId);
    Optional<Rating> findByTarget_BookingId(Long bookingId);
    boolean          existsByTarget_BookingId(Long bookingId);
}
