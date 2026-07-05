package com.winx.rating.application;

import com.winx.rating.domain.Rating;
import com.winx.rating.domain.RatingTarget;
import com.winx.rating.domain.Review;
import com.winx.rating.domain.Score;
import com.winx.rating.infrastructure.RatingRepository;
import com.winx.rating.infrastructure.client.BookingFeignClient;
import com.winx.rating.infrastructure.client.dto.BookingStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RatingSubmissionService {

    private final RatingRepository ratingRepository;
    private final BookingFeignClient bookingClient;

    /**
     * Submits a rating for a completed booking.
     *
     * Invariants enforced:
     *  - scores must be 1–5 (Score.of throws if out of range)
     *  - exactly one rating per bookingId
     *  - booking must be COMPLETED (verified via bc03-booking through Feign;
     *    if bc03 is unreachable the circuit opens and the fallback returns COMPLETED
     *    so ratings degrade gracefully rather than blocking all users)
     */
    public Rating submitRating(Long bookingId, Long userId,
                               Long vehicleId, Long providerId,
                               int vehicleScore, int providerScore,
                               String comment) {

        if (ratingRepository.existsByTarget_BookingId(bookingId)) {
            throw new IllegalStateException(
                    "Booking " + bookingId + " has already been rated.");
        }

        BookingStatusResponse booking = bookingClient.getBooking(bookingId);
        if (!"COMPLETED".equals(booking.status())) {
            throw new IllegalStateException(
                    "Booking " + bookingId + " cannot be rated — status is " + booking.status() + ".");
        }

        RatingTarget target = new RatingTarget(vehicleId, providerId, bookingId);
        Review review = new Review(Score.of(vehicleScore), Score.of(providerScore), comment);
        return ratingRepository.save(Rating.create(userId, target, review));
    }
}
