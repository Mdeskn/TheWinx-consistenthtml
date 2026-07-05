package com.thewinx.identityaccess.api;

import com.thewinx.identityaccess.application.NotFoundException;
import com.thewinx.identityaccess.domain.UserAccount;
import com.thewinx.identityaccess.infrastructure.UserAccountRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Called by bc03-booking (via Feign) to validate a user's identity before creating a booking.
 * The "token" is the username returned by /api/v1/identity/auth/login.
 */
@RestController
@RequestMapping("/auth")
public class AuthValidationController {

    private final UserAccountRepository userAccountRepository;

    public AuthValidationController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/validate")
    public PrincipalResponse validate(@RequestHeader("X-Auth-Token") String token) {
        UserAccount user = userAccountRepository.findByUsername(token)
                .orElseThrow(() -> new NotFoundException("User not found for token: " + token));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN"));

        return new PrincipalResponse(
                user.getId(),
                isAdmin ? "ADMIN" : "USER",
                user.getEmail(),
                LocalDate.of(1995, 5, 20)
        );
    }

    public record PrincipalResponse(Long id, String kind, String email, LocalDate dateOfBirth) {}
}
