package com.thewinx.identityaccess.api;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thewinx.identityaccess.api.dto.AuthRequest;
import com.thewinx.identityaccess.api.dto.AuthResponse;
import com.thewinx.identityaccess.api.dto.PermissionCheckResponse;
import com.thewinx.identityaccess.api.dto.RegisterUserRequest;
import com.thewinx.identityaccess.api.dto.RoleAssignmentRequest;
import com.thewinx.identityaccess.api.dto.UpdateUserRequest;
import com.thewinx.identityaccess.api.dto.UserResponse;
import com.thewinx.identityaccess.api.dto.VehicleBookingRequest;
import com.thewinx.identityaccess.application.FleetService;
import com.thewinx.identityaccess.application.IdentityAccessService;
import com.thewinx.identityaccess.infrastructure.fleet.FleetClient;
import com.thewinx.identityaccess.infrastructure.fleet.dto.VehicleDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/identity")
public class IdentityAccessRestController {

    private final IdentityAccessService identityAccessService;
    private final FleetService fleetService;
    private final FleetClient fleetClient;

    public IdentityAccessRestController(IdentityAccessService identityAccessService, FleetService fleetService, FleetClient fleetClient) {
        this.identityAccessService = identityAccessService;
        this.fleetService = fleetService;
        this.fleetClient = fleetClient;
    }

    @PostMapping("/users")
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        return UserResponse.from(identityAccessService.register(request.getUsername(), request.getEmail(), request.getPassword()));
    }

    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return identityAccessService.listUsers().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/users/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        return UserResponse.from(identityAccessService.getUser(userId));
    }

    @PutMapping("/users/{userId}")
    public UserResponse updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
        return UserResponse.from(identityAccessService.updateUser(userId, request.getUsername(), request.getEmail()));
    }

    @DeleteMapping("/users/{userId}")
    public UserResponse deactivateUser(@PathVariable Long userId) {
        return UserResponse.from(identityAccessService.deactivateUser(userId));
    }

    @PostMapping("/users/{userId}/roles")
    public UserResponse assignRole(@PathVariable Long userId, @Valid @RequestBody RoleAssignmentRequest request) {
        return UserResponse.from(identityAccessService.assignRole(userId, request.getRoleName()));
    }

    @DeleteMapping("/users/{userId}/roles")
    public UserResponse revokeRole(@PathVariable Long userId, @Valid @RequestBody RoleAssignmentRequest request) {
        return UserResponse.from(identityAccessService.revokeRole(userId, request.getRoleName()));
    }

    @PostMapping("/auth/login")
    public AuthResponse authenticate(@Valid @RequestBody AuthRequest request) {
        return AuthResponse.from(identityAccessService.authenticate(request.getUsername(), request.getPassword()));
    }

    @GetMapping("/permissions/check")
    public PermissionCheckResponse checkPermission(@RequestParam Long userId, @RequestParam String permission) {
        return new PermissionCheckResponse(userId, permission, identityAccessService.hasPermission(userId, permission));
    }

    @GetMapping("/fleet/vehicles")
    public List<VehicleDto> listVehicles() {
        return fleetClient.getAllVehicles();
    }

    @GetMapping("/fleet/bookings")
    public List<FleetService.BookingView> listBookings(@RequestParam(required = false) String username) {
        if (username == null || username.isBlank()) {
            return fleetService.listBookings();
        }
        return fleetService.listBookingsByUsername(username);
    }

    @PostMapping("/fleet/bookings")
    public FleetService.BookingView createBooking(@Valid @RequestBody VehicleBookingRequest request) {
        return fleetService.createBooking(
            request.getVehicleId(),
            request.getUsername(),
            request.getPickupDate(),
            request.getReturnDate()
        );
    }

    @PostMapping("/fleet/bookings/{bookingId}/cancel")
    public FleetService.BookingView cancelBooking(@PathVariable Long bookingId) {
        return fleetService.cancelBooking(bookingId);
    }
}
