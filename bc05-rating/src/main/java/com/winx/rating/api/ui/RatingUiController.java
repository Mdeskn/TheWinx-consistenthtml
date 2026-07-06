package com.winx.rating.api.ui;

import com.winx.rating.api.dto.RatingResponse;
import com.winx.rating.application.RatingQueryService;
import com.winx.rating.application.RatingSubmissionService;
import com.winx.rating.infrastructure.client.FleetFeignClient;
import com.winx.rating.infrastructure.client.dto.VehicleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingUiController {

    private final RatingSubmissionService submissionService;
    private final RatingQueryService queryService;
    private final FleetFeignClient fleetFeignClient;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("ratings",
                queryService.getAll().stream().map(RatingResponse::from).toList());
        return "ratings/list";
    }

    @GetMapping("/submit")
    public String submitForm(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Long providerId,
            Model model) {
        RatingForm form = new RatingForm();
        if (bookingId  != null) form.setBookingId(bookingId);
        if (userId     != null) form.setUserId(userId);
        if (vehicleId  != null) form.setVehicleId(vehicleId);
        if (providerId != null) form.setProviderId(providerId);
        model.addAttribute("form", form);
        return "ratings/submit";
    }

    @PostMapping("/submit")
    public String submitRating(@Valid @ModelAttribute("form") RatingForm form,
                               BindingResult binding,
                               Model model,
                               RedirectAttributes flash) {
        if (binding.hasErrors()) {
            return "ratings/submit";
        }
        try {
            submissionService.submitRating(
                    form.getBookingId(), form.getUserId(),
                    form.getVehicleId(), form.getProviderId(),
                    form.getVehicleScore(), form.getProviderScore(),
                    form.getComment());
            flash.addFlashAttribute("success", "Rating submitted successfully!");
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "ratings/submit";
        }
        return "redirect:/ratings";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("rating", RatingResponse.from(queryService.getById(id)));
        } catch (NoSuchElementException e) {
            return "redirect:/ratings";
        }
        return "ratings/detail";
    }

    @GetMapping("/vehicle/{vehicleId}")
    public String byVehicle(@PathVariable Long vehicleId, Model model) {
        model.addAttribute("ratings",
                queryService.getVehicleRatings(vehicleId).stream()
                        .map(RatingResponse::from).toList());
        model.addAttribute("averageScore", queryService.getAverageVehicleScore(vehicleId));

        VehicleResponse vehicle;
        try {
            vehicle = fleetFeignClient.getVehicle(vehicleId);
        } catch (Exception e) {
            vehicle = new VehicleResponse(vehicleId, null, "Vehicle #" + vehicleId, "UNKNOWN",
                    "Fleet Management is currently unavailable.", "UNKNOWN", null, null, null, null);
        }
        model.addAttribute("vehicle", vehicle);
        return "ratings/vehicle";
    }

    @GetMapping("/provider/{providerId}")
    public String byProvider(@PathVariable Long providerId, Model model) {
        model.addAttribute("ratings",
                queryService.getProviderRatings(providerId).stream()
                        .map(RatingResponse::from).toList());
        model.addAttribute("filter", "Provider #" + providerId);
        model.addAttribute("averageScore", queryService.getAverageProviderScore(providerId));
        return "ratings/list";
    }
}
