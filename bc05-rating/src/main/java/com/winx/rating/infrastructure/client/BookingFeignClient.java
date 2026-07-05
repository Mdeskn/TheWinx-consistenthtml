package com.winx.rating.infrastructure.client;

import com.winx.rating.infrastructure.client.dto.BookingStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bc03-booking", url = "${booking.service.url:http://localhost:8083}", fallback = BookingFeignFallback.class)
public interface BookingFeignClient {

    @GetMapping("/bookings/{id}")
    BookingStatusResponse getBooking(@PathVariable("id") Long bookingId);
}
