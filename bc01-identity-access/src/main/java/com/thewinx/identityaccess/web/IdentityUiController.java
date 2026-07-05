package com.thewinx.identityaccess.web;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thewinx.identityaccess.api.dto.AuthResponse;
import com.thewinx.identityaccess.api.dto.UserResponse;
import com.thewinx.identityaccess.application.FleetService;
import com.thewinx.identityaccess.application.IdentityAccessService;
import com.thewinx.identityaccess.application.UnauthorizedException;
import com.thewinx.identityaccess.infrastructure.fleet.FleetClient;
import com.thewinx.identityaccess.infrastructure.fleet.dto.VehicleDto;
import java.util.List;

@Controller
public class IdentityUiController {

    private final IdentityAccessService identityAccessService;
    private final FleetService fleetService;
    private final FleetClient fleetClient;

    public IdentityUiController(IdentityAccessService identityAccessService, FleetService fleetService, FleetClient fleetClient) {
        this.identityAccessService = identityAccessService;
        this.fleetService = fleetService;
        this.fleetClient = fleetClient;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("users", identityAccessService.listUsers().stream().map(UserResponse::from).toList());
        var bookings = fleetService.listBookings();
        model.addAttribute("fleetBookings", bookings);
        Map<Long, FleetService.BookingView> activeBookingsByVehicle = bookings.stream()
            .filter(booking -> "CONFIRMED".equals(booking.getStatus()) || "PENDING".equals(booking.getStatus()))
            .collect(Collectors.toMap(FleetService.BookingView::getVehicleId, Function.identity(), (left, right) -> left));
        model.addAttribute("activeBookingsByVehicle", activeBookingsByVehicle);

        List<VehicleDto> vehicles;
        try {
            vehicles = fleetClient.getAllVehicles();
        } catch (Exception e) {
            vehicles = List.of();
        }
        model.addAttribute("vehicles", vehicles);

        return "index";
    }

    @PostMapping("/ui/users")
    public String createUser(@RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String email,
                             @RequestParam String username,
                             @RequestParam String phoneNumber,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             Model model,
                             @RequestParam(defaultValue = "/") String redirectTo) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Password and confirm password must match.");
            return "register";
        }

        identityAccessService.register(username, email, password);
        if (!redirectTo.startsWith("/")) {
            redirectTo = "/";
        }
        return "redirect:" + redirectTo;
    }

    @PostMapping("/ui/users/deactivate")
    public String deactivateUser(@RequestParam Long userId) {
        identityAccessService.deactivateUser(userId);
        return "redirect:/";
    }

    @PostMapping("/ui/users/assign-role")
    public String assignRole(@RequestParam Long userId, @RequestParam String roleName) {
        identityAccessService.assignRole(userId, roleName);
        return "redirect:/";
    }

    @GetMapping("/ui/users/{id}/edit")
    public String editUserPage(@PathVariable Long id, Model model) {
        model.addAttribute("user", UserResponse.from(identityAccessService.getUser(id)));
        return "edit";
    }

    @PostMapping("/ui/users/{id}")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String username,
                             @RequestParam String email,
                             Model model) {
        identityAccessService.updateUser(id, username, email);
        return "redirect:/";
    }

    @GetMapping("/ui/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/ui/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/ui/dashboard")
    public String userDashboard(Model model) {
        String username = "demo.user";
        model.addAttribute("defaultUsername", username);

        List<VehicleDto> vehicles;
        try {
            vehicles = fleetClient.getAllVehicles();
        } catch (Exception e) {
            vehicles = List.of();
        }
        List<FleetService.BookingView> bookings = fleetService.listBookingsByUsername(username);

        long statAvailable = vehicles.stream().filter(VehicleDto::isAvailable).count();
        long statMyBookings = bookings.size();
        long statActive = bookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()) || "PENDING".equalsIgnoreCase(b.getStatus()))
                .count();
        long statCompleted = bookings.stream()
                .filter(b -> "COMPLETED".equalsIgnoreCase(b.getStatus()))
                .count();

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("bookings", bookings);
        model.addAttribute("statAvailable", statAvailable);
        model.addAttribute("statMyBookings", statMyBookings);
        model.addAttribute("statActive", statActive);
        model.addAttribute("statCompleted", statCompleted);

        return "user-dashboard";
    }

    @PostMapping("/ui/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model) {
        try {
            AuthResponse response = AuthResponse.from(identityAccessService.authenticate(username, password));
            // Admins have USER_MANAGE or ROLE_MANAGE permission; regular users do not
            boolean isAdmin = response.getPermissions().contains("ROLE_MANAGE")
                    || response.getPermissions().contains("USER_MANAGE");
            return isAdmin ? "redirect:/" : "redirect:/ui/dashboard";
        } catch (UnauthorizedException e) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }
}
