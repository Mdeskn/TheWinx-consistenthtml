package com.winx.rating.api.dto;

import com.winx.rating.domain.Rating;

import java.time.LocalDateTime;

public record RatingResponse(
        Long ratingId,
        Long userId,
        LocalDateTime createdAt,
        Long bookingId,
        Long vehicleId,
        Long providerId,
        int vehicleScore,
        int providerScore,
        String comment
) {
    public static RatingResponse from(Rating r) {
        return new RatingResponse(
                r.getRatingId(),
                r.getUserId(),
                r.getCreatedAt(),
                r.getTarget().getBookingId(),
                r.getTarget().getVehicleId(),
                r.getTarget().getProviderId(),
                r.getReview().getVehicleScore().getValue(),
                r.getReview().getProviderScore().getValue(),
                r.getReview().getComment()
        );
    }
}
