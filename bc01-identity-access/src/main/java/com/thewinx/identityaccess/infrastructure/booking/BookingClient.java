package com.thewinx.identityaccess.infrastructure.booking;

import com.thewinx.identityaccess.infrastructure.booking.dto.BookingCreateRequest;
import com.thewinx.identityaccess.infrastructure.booking.dto.BookingDto;
import com.thewinx.identityaccess.infrastructure.booking.dto.EndRideRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "bc03-booking", url = "${booking.service.url:http://localhost:8083}")
public interface BookingClient {

    @PostMapping("/bookings")
    BookingDto create(@RequestHeader("X-Auth-Token") String token,
                      @RequestBody BookingCreateRequest request);

    @GetMapping("/bookings")
    List<BookingDto> findByUser(@RequestParam("userId") Long userId);

    @PostMapping("/bookings/{id}/cancel")
    BookingDto cancel(@PathVariable("id") Long id);

    @PostMapping("/bookings/{id}/end")
    BookingDto endRide(@PathVariable("id") Long id, @RequestBody EndRideRequest request);
}
