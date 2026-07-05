package com.thewinx.identityaccess.contracts;

public class ProviderAccessResponse {

	private final Long userId;
	private final boolean canAccess;
	private final String status;

	public ProviderAccessResponse(Long userId, boolean canAccess, String status) {
		this.userId = userId;
		this.canAccess = canAccess;
		this.status = status;
	}

	public Long getUserId() {
		return userId;
	}

	public boolean isCanAccess() {
		return canAccess;
	}

	public String getStatus() {
		return status;
	}
}
