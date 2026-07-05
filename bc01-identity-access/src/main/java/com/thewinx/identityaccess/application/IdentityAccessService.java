package com.thewinx.identityaccess.application;

import com.thewinx.identityaccess.domain.AccountStatus;
import com.thewinx.identityaccess.domain.Role;
import com.thewinx.identityaccess.domain.UserAccount;
import com.thewinx.identityaccess.infrastructure.RoleRepository;
import com.thewinx.identityaccess.infrastructure.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class IdentityAccessService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;

    public IdentityAccessService(UserAccountRepository userAccountRepository, RoleRepository roleRepository) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
    }

    public UserAccount register(String username, String email, String plainPassword) {
        userAccountRepository.findByUsername(username)
            .ifPresent(existing -> {
                throw new DuplicateResourceException("Username already exists");
            });

        userAccountRepository.findByEmail(email)
            .ifPresent(existing -> {
                throw new DuplicateResourceException("Email already exists");
            });

        UserAccount userAccount = new UserAccount(username, email, hashPassword(plainPassword));
        Role defaultRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new NotFoundException("Default role USER is missing"));
        userAccount.assignRole(defaultRole);
        return userAccountRepository.save(userAccount);
    }

    @Transactional(readOnly = true)
    public List<UserAccount> listUsers() {
        return userAccountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UserAccount getUser(Long userId) {
        Objects.requireNonNull(userId, "userId cannot be null");
        return userAccountRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public UserAccount updateUser(Long userId, String username, String email) {
        UserAccount userAccount = getUser(userId);
        userAccount.updateProfile(username, email);
        return userAccount;
    }

    public UserAccount deactivateUser(Long userId) {
        UserAccount userAccount = getUser(userId);
        userAccount.setStatus(AccountStatus.INACTIVE);
        return userAccount;
    }

    public UserAccount assignRole(Long userId, String roleName) {
        UserAccount userAccount = getUser(userId);
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
        userAccount.assignRole(role);
        return userAccount;
    }

    public UserAccount revokeRole(Long userId, String roleName) {
        UserAccount userAccount = getUser(userId);
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
        userAccount.revokeRole(role);
        return userAccount;
    }

    @Transactional(readOnly = true)
    public AuthenticationResult authenticate(String username, String plainPassword) {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (userAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        if (!userAccount.getPasswordHash().equals(hashPassword(plainPassword))) {
            throw new UnauthorizedException("Invalid username or password");
        }

        Set<String> permissions = userAccount.getRoles()
            .stream()
            .flatMap(role -> role.getPermissions().stream())
            .collect(java.util.stream.Collectors.toSet());

        return new AuthenticationResult(userAccount.getId(), userAccount.getUsername(), permissions);
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String permission) {
        UserAccount userAccount = getUser(userId);
        return userAccount.getRoles().stream().flatMap(role -> role.getPermissions().stream())
            .anyMatch(permission::equalsIgnoreCase);
    }

    private String hashPassword(String plainText) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to hash password", ex);
        }
    }
}
