package com.thewinx.identityaccess.api;

import com.thewinx.identityaccess.application.NotFoundException;
import com.thewinx.identityaccess.infrastructure.UserAccountRepository;
import com.thewinx.identityaccess.infrastructure.booking.BookingClient;
import com.thewinx.identityaccess.infrastructure.booking.dto.BookingCreateRequest;
import com.thewinx.identityaccess.infrastructure.booking.dto.BookingDto;
import com.thewinx.identityaccess.infrastructure.booking.dto.EndRideRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingProxyController {

    private static final double DEFAULT_LAT = 51.5178;
    private static final double DEFAULT_LON = 7.4590;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final BookingClient bookingClient;
    private final UserAccountRepository userAccountRepository;

    public BookingProxyController(BookingClient bookingClient, UserAccountRepository userAccountRepository) {
        this.bookingClient = bookingClient;
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping("/create")
    public Map<String, Object> createBooking(@RequestBody Map<String, Object> body) {
        String username      = (String) body.get("username");
        Long vehicleId       = ((Number) body.get("vehicleId")).longValue();
        String paymentMethod = body.get("paymentMethod") instanceof String s ? s : "CARD";

        userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        BookingDto booking = bookingClient.create(username,
                new BookingCreateRequest(vehicleId, DEFAULT_LAT, DEFAULT_LON, paymentMethod));

        return toView(booking);
    }

    @GetMapping("/my")
    public List<Map<String, Object>> myBookings(@RequestParam String username) {
        Long userId = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username))
                .getId();

        return bookingClient.findByUser(userId).stream()
                .map(this::toView)
                .toList();
    }

    @PostMapping("/{id}/cancel")
    public Map<String, Object> cancel(@PathVariable Long id) {
        return toView(bookingClient.cancel(id));
    }

    @PostMapping("/{id}/end")
    public Map<String, Object> endRide(@PathVariable Long id) {
        double distanceKm = Math.round((0.5 + Math.random() * 7.5) * 100.0) / 100.0;
        double bearing    = Math.random() * 2 * Math.PI;
        double endLat     = DEFAULT_LAT + (distanceKm * Math.cos(bearing)) / 111.32;
        double endLon     = DEFAULT_LON + (distanceKm * Math.sin(bearing)) / (111.32 * Math.cos(Math.toRadians(DEFAULT_LAT)));

        return toView(bookingClient.endRide(id, new EndRideRequest(endLat, endLon, distanceKm)));
    }

    private Map<String, Object> toView(BookingDto b) {
        String dashboardStatus = switch ((b.getStatus() != null ? b.getStatus() : "ACTIVE").toUpperCase()) {
            case "ACTIVE"    -> "CONFIRMED";
            case "COMPLETED" -> "COMPLETED";
            case "CANCELLED" -> "CANCELLED";
            default          -> "PENDING";
        };

        String vehicleName = b.getVehicleType() != null
                ? vehicleTypeLabel(b.getVehicleType()) + " #" + b.getVehicleId()
                : "Vehicle #" + b.getVehicleId();

        String startDate = b.getStartTime() != null ? b.getStartTime().format(DATE_FMT) : "-";
        String endDate   = b.getEndTime()   != null ? b.getEndTime().format(DATE_FMT)   : "Active";

        return Map.of(
                "id",          b.getBookingId(),
                "vehicleId",   b.getVehicleId()  != null ? b.getVehicleId()  : 0L,
                "providerId",  b.getProviderId() != null ? b.getProviderId() : 0L,
                "vehicleName", vehicleName,
                "plate",       "BC03-" + b.getBookingId(),
                "pickupDate",  startDate,
                "returnDate",  endDate,
                "status",      dashboardStatus,
                "totalCost",   b.getTotalCost() != null ? b.getTotalCost() : 0
        );
    }

    private String vehicleTypeLabel(String type) {
        return switch (type.toUpperCase()) {
            case "E_SCOOTER" -> "E-Scooter";
            case "BICYCLE"   -> "Bicycle";
            case "E_BIKE"    -> "E-Bike";
            case "E_CAR"     -> "E-Car";
            default          -> type;
        };
    }
}
