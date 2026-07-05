package com.thewinx.identityaccess.contracts;

public class BookingIdentityCheckResponse {

	private final boolean verified;
	private final String reason;

	public BookingIdentityCheckResponse(boolean verified, String reason) {
		this.verified = verified;
		this.reason = reason;
	}

	public boolean isVerified() {
		return verified;
	}

	public String getReason() {
		return reason;
	}
}
