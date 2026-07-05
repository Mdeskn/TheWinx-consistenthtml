package com.winx.rating.infrastructure.client;

import com.winx.rating.infrastructure.client.dto.BookingStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Called by Resilience4j when the bc03-booking circuit is open or the call fails.
 * Returns COMPLETED so the rating service degrades gracefully rather than blocking all ratings.
 */
@Slf4j
@Component
public class BookingFeignFallback implements BookingFeignClient {

    @Override
    public BookingStatusResponse getBooking(Long bookingId) {
        log.warn("bc03-booking unreachable - circuit open. Allowing rating for booking {}.", bookingId);
        return new BookingStatusResponse(bookingId, null, "COMPLETED");
    }
}
