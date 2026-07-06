package com.thewinx.identityaccess.web;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
import com.thewinx.identityaccess.infrastructure.UserAccountRepository;
import com.thewinx.identityaccess.infrastructure.fleet.FleetClient;
import com.thewinx.identityaccess.infrastructure.fleet.dto.VehicleDto;
import java.util.List;

@Controller
public class IdentityUiController {

    private final IdentityAccessService identityAccessService;
    private final FleetService fleetService;
    private final FleetClient fleetClient;
    private final UserAccountRepository userAccountRepository;

    public IdentityUiController(IdentityAccessService identityAccessService, FleetService fleetService,
                                FleetClient fleetClient, UserAccountRepository userAccountRepository) {
        this.identityAccessService = identityAccessService;
        this.fleetService = fleetService;
        this.fleetClient = fleetClient;
        this.userAccountRepository = userAccountRepository;
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
                             @RequestParam(required = false) String dateOfBirth,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             Model model,
                             @RequestParam(defaultValue = "/") String redirectTo) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Password and confirm password must match.");
            return "register";
        }

        java.time.LocalDate dob = null;
        if (dateOfBirth != null && !dateOfBirth.isBlank()) {
            try { dob = java.time.LocalDate.parse(dateOfBirth); } catch (Exception ignored) {}
        }

        identityAccessService.register(username, email, password, firstName, lastName, phoneNumber, dob);
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
    public String userDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        Long userId     = (Long)   session.getAttribute("userId");
        if (username == null) return "redirect:/ui/login";

        model.addAttribute("defaultUsername", username);
        model.addAttribute("currentUserId", userId);

        List<VehicleDto> vehicles;
        try {
            vehicles = fleetClient.getAllVehicles();
        } catch (Exception e) {
            vehicles = List.of();
        }

        long statAvailable = vehicles.stream().filter(VehicleDto::isAvailable).count();

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("bookings", List.of());
        model.addAttribute("statAvailable", statAvailable);
        model.addAttribute("statMyBookings", 0);
        model.addAttribute("statActive", 0);
        model.addAttribute("statCompleted", 0);

        return "user-dashboard";
    }

    @GetMapping("/ui/logout")
    public String logout(HttpSession session, HttpServletResponse httpResponse) {
        session.invalidate();
        Cookie userCookie = new Cookie("winx-username", "");
        userCookie.setPath("/");
        userCookie.setMaxAge(0);
        httpResponse.addCookie(userCookie);
        return "redirect:/ui/login";
    }

    @PostMapping("/ui/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        HttpServletResponse httpResponse,
                        Model model) {
        try {
            AuthResponse response = AuthResponse.from(identityAccessService.authenticate(username, password));
            boolean isAdmin = response.getPermissions().contains("ROLE_MANAGE")
                    || response.getPermissions().contains("USER_MANAGE");

            Long userId = userAccountRepository.findByUsername(username)
                    .map(u -> u.getId()).orElse(null);
            session.setAttribute("username", username);
            session.setAttribute("userId", userId);

            Cookie userCookie = new Cookie("winx-username", username);
            userCookie.setPath("/");
            userCookie.setMaxAge(86400);
            httpResponse.addCookie(userCookie);

            return isAdmin ? "redirect:/" : "redirect:/ui/dashboard";
        } catch (UnauthorizedException e) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }
}
