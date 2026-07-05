package com.thewinx.identityaccess.application;

import java.util.Set;

public class AuthenticationResult {

	private final Long userId;
	private final String username;
	private final Set<String> permissions;

	public AuthenticationResult(Long userId, String username, Set<String> permissions) {
		this.userId = userId;
		this.username = username;
		this.permissions = permissions;
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
}
