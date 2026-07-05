package com.thewinx.identityaccess.api.dto;

public class PermissionCheckResponse {

	private final Long userId;
	private final String permission;
	private final boolean granted;

	public PermissionCheckResponse(Long userId, String permission, boolean granted) {
		this.userId = userId;
		this.permission = permission;
		this.granted = granted;
	}

	public Long getUserId() {
		return userId;
	}

	public String getPermission() {
		return permission;
	}

	public boolean isGranted() {
		return granted;
	}
}
