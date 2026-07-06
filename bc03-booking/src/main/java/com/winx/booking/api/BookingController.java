package com.winx.booking.api;

import com.winx.booking.api.dto.BookingCreateRequest;
import com.winx.booking.api.dto.BookingDto;
import com.winx.booking.api.dto.BookingStatusDto;
import com.winx.booking.api.dto.EndRideRequest;
import com.winx.booking.api.dto.VehicleDto;
import com.winx.booking.application.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Ride booking lifecycle")
public class BookingController {

    private final BookingService service;

    @PostMapping("/bookings")
    public ResponseEntity<BookingDto> create(@RequestHeader("X-Auth-Token") String token,
                                             @Valid @RequestBody BookingCreateRequest request) {
        BookingDto dto = BookingDto.from(service.createBooking(token, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/bookings/{id}")
    public BookingDto getById(@PathVariable Long id) {
        return BookingDto.from(service.findById(id));
    }

    @GetMapping("/bookings")
    public List<BookingDto> getByUser(@RequestParam Long userId) {
        return service.findByUser(userId).stream().map(BookingDto::from).toList();
    }

    @PostMapping("/bookings/{id}/cancel")
    public BookingDto cancel(@PathVariable Long id) {
        return BookingDto.from(service.cancelBooking(id));
    }

    @PostMapping("/bookings/{id}/end")
    public BookingDto end(@PathVariable Long id, @Valid @RequestBody EndRideRequest request) {
        return BookingDto.from(service.endBooking(id, request));
    }

    @GetMapping("/bookings/{id}/status")
    public BookingStatusDto getStatus(@PathVariable Long id) {
        return BookingStatusDto.from(service.findById(id));
    }

    @GetMapping("/vehicles/search")
    public List<VehicleDto> searchVehicles(@RequestParam double lat,
                                           @RequestParam double lon,
                                           @RequestParam(defaultValue = "2.0") double radiusKm,
                                           @RequestParam(required = false) String type,
                                           @RequestParam(required = false) BigDecimal maxPrice,
                                           @RequestParam(required = false) Integer minPersons) {
        return service.searchVehicles(lat, lon, radiusKm, type, maxPrice, minPersons);
    }
}
