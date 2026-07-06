package com.winx.booking.api.ui;

import com.winx.booking.api.dto.BookingCreateRequest;
import com.winx.booking.api.dto.BookingDto;
import com.winx.booking.api.dto.EndRideRequest;
import com.winx.booking.application.BookingService;
import com.winx.booking.domain.Booking;
import com.winx.booking.exception.DomainException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ui")
@RequiredArgsConstructor
public class BookingWebController {

    private final BookingService service;

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String location,
                         @RequestParam(required = false, defaultValue = "5.0") Double radiusKm,
                         @RequestParam(required = false) String type,
                         @RequestParam(required = false) java.math.BigDecimal maxPrice,
                         @RequestParam(required = false) Integer minPersons,
                         @RequestParam(required = false, defaultValue = "CARD") String paymentMethod,
                         @CookieValue(name = "winx-username", required = false) String cookieUsername,
                         Model model) {
        DemoLocations.Location loc = DemoLocations.find(location);
        String resolvedType = (type != null && !type.isBlank()) ? type : null;
        if (loc != null) {
            model.addAttribute("results", service.searchVehicles(loc.latitude(), loc.longitude(), radiusKm, resolvedType, maxPrice, minPersons));
            model.addAttribute("lat", loc.latitude());
            model.addAttribute("lon", loc.longitude());
        }
        model.addAttribute("locations", DemoLocations.all());
        model.addAttribute("selectedLocation", location);
        model.addAttribute("radiusKm", radiusKm);
        model.addAttribute("type", type);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minPersons", minPersons);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("loggedInUser", cookieUsername);
        return "search";
    }

    @PostMapping("/bookings/create")
    public String create(@RequestParam(required = false) String token,
                         @CookieValue(name = "winx-username", required = false) String cookieUsername,
                         @RequestParam Long vehicleId,
                         @RequestParam Double startLatitude,
                         @RequestParam Double startLongitude,
                         @RequestParam(required = false, defaultValue = "CARD") String paymentMethod,
                         RedirectAttributes ra) {
        String effectiveToken = (token != null && !token.isBlank()) ? token : cookieUsername;
        if (effectiveToken == null || effectiveToken.isBlank()) {
            ra.addFlashAttribute("error", "You must be logged in at BC-01 to book a vehicle.");
            return "redirect:/ui/search";
        }
        try {
            Booking booking = service.createBooking(effectiveToken,
                    new BookingCreateRequest(vehicleId, startLatitude, startLongitude, paymentMethod));
            return "redirect:/ui/bookings?userId=" + booking.getUserId();
        } catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ui/search";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Booking failed: " + e.getMessage());
            return "redirect:/ui/search";
        }
    }

    @GetMapping("/bookings")
    public String bookings(@RequestParam(required = false) Long userId, Model model) {
        List<BookingDto> bookings = userId != null
                ? service.findByUser(userId).stream().map(BookingDto::from).toList()
                : service.findAll().stream().map(BookingDto::from).toList();
        model.addAttribute("bookings", bookings);
        model.addAttribute("userId", userId);
        return "bookings";
    }

    @GetMapping("/bookings/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("booking", BookingDto.from(service.findById(id)));
        return "booking-detail";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.cancelBooking(id);
        } catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ui/bookings/" + id;
    }

    @PostMapping("/bookings/{id}/end")
    public String end(@PathVariable Long id, RedirectAttributes ra) {
        try {
            BookingDto booking = BookingDto.from(service.findById(id));
            service.endBooking(id, simulateEndTelemetry(booking));
        } catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not end ride: " + e.getMessage());
        }
        return "redirect:/ui/bookings/" + id;
    }

    private EndRideRequest simulateEndTelemetry(BookingDto booking) {
        double startLat = booking.startLatitude() != null ? booking.startLatitude() : 51.5178;
        double startLon = booking.startLongitude() != null ? booking.startLongitude() : 7.4590;
        double distanceKm = 0.5 + Math.random() * 7.5;
        double bearing = Math.random() * 2 * Math.PI;
        double endLat = startLat + (distanceKm * Math.cos(bearing)) / 111.32;
        double endLon = startLon + (distanceKm * Math.sin(bearing)) / (111.32 * Math.cos(Math.toRadians(startLat)));
        double roundedDistance = Math.round(distanceKm * 100.0) / 100.0;
        return new EndRideRequest(endLat, endLon, roundedDistance);
    }
}
