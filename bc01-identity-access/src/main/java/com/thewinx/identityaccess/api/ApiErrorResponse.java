package com.thewinx.identityaccess.api;

import java.time.Instant;

public class ApiErrorResponse {

	private final String message;
	private final Instant timestamp;

	public ApiErrorResponse(String message, Instant timestamp) {
		this.message = message;
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public Instant getTimestamp() {
		return timestamp;
	}
}
