package com.thewinx.identityaccess.api.dto;

import java.util.Set;

import com.thewinx.identityaccess.application.AuthenticationResult;

public class AuthResponse {

    private final Long userId;
    private final String username;
    private final Set<String> permissions;
    private final String tokenType;

    public AuthResponse(Long userId, String username, Set<String> permissions, String tokenType) {
        this.userId = userId;
        this.username = username;
        this.permissions = permissions;
        this.tokenType = tokenType;
    }

    public static AuthResponse from(AuthenticationResult authenticationResult) {
        return new AuthResponse(
            authenticationResult.getUserId(),
            authenticationResult.getUsername(),
            authenticationResult.getPermissions(),
            "mock-session-token"
        );
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public String getTokenType() {
        return tokenType;
    }
}
