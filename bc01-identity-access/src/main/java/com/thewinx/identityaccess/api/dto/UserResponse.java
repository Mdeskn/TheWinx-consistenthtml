package com.thewinx.identityaccess.api.dto;

import com.thewinx.identityaccess.domain.AccountStatus;
import com.thewinx.identityaccess.domain.UserAccount;

import java.util.Set;
import java.util.stream.Collectors;

public class UserResponse {

    private final Long id;
    private final String username;
    private final String email;
    private final AccountStatus status;
    private final Set<String> roles;

    public UserResponse(Long id, String username, String email, AccountStatus status, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
        this.roles = roles;
    }

    public static UserResponse from(UserAccount user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getStatus(),
            user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet())
        );
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
