package com.thewinx.identityaccess.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.thewinx.identityaccess.domain.Role;
import com.thewinx.identityaccess.domain.UserAccount;
import com.thewinx.identityaccess.infrastructure.RoleRepository;
import com.thewinx.identityaccess.infrastructure.UserAccountRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;

    public DataSeeder(RoleRepository roleRepository, UserAccountRepository userAccountRepository) {
        this.roleRepository = roleRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public void run(String... args) {
        Role userRole = roleRepository.findByName("USER")
            .orElseGet(() -> roleRepository.save(new Role("USER", Set.of("BOOKING_READ", "PROFILE_EDIT"))));
        Role adminRole = roleRepository.findByName("ADMIN")
            .orElseGet(() -> roleRepository.save(new Role("ADMIN", Set.of("USER_MANAGE", "ROLE_MANAGE", "AUDIT_READ"))));
        Role providerRole = roleRepository.findByName("PROVIDER")
            .orElseGet(() -> roleRepository.save(new Role("PROVIDER", Set.of("FLEET_MANAGE", "VEHICLE_ADD", "VEHICLE_EDIT", "VEHICLE_DELETE"))));

        if (userAccountRepository.findByUsername("demo.user").isEmpty()) {
            UserAccount demoUser = new UserAccount("demo.user", "demo.user@thewinx.com", sha256("demo123"),
                    "Alex", "Demo", "491701234567", LocalDate.of(1995, 6, 15));
            demoUser.assignRole(userRole);
            userAccountRepository.save(demoUser);
        }

        if (userAccountRepository.findByUsername("demo.admin").isEmpty()) {
            UserAccount demoAdmin = new UserAccount("demo.admin", "demo.admin@thewinx.com", sha256("admin123"),
                    "Admin", "Winx", "491709876543", LocalDate.of(1985, 3, 22));
            demoAdmin.assignRole(userRole);
            demoAdmin.assignRole(adminRole);
            userAccountRepository.save(demoAdmin);
        }

        if (userAccountRepository.findByUsername("demo.provider").isEmpty()) {
            UserAccount demoProvider = new UserAccount("demo.provider", "demo.provider@thewinx.com", sha256("provider123"),
                    "Provider", "Winx", "491705550001", LocalDate.of(1980, 9, 10));
            demoProvider.assignRole(userRole);
            demoProvider.assignRole(providerRole);
            userAccountRepository.save(demoProvider);
        }
    }

    private String sha256(String plainText) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to hash password", ex);
        }
    }
}
