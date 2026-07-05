package com.thewinx.identityaccess.contracts;

public class PaymentEligibilityResponse {

	private final Long userId;
	private final boolean eligible;
	private final String status;

	public PaymentEligibilityResponse(Long userId, boolean eligible, String status) {
		this.userId = userId;
		this.eligible = eligible;
		this.status = status;
	}

	public Long getUserId() {
		return userId;
	}

	public boolean isEligible() {
		return eligible;
	}

	public String getStatus() {
		return status;
	}
}
