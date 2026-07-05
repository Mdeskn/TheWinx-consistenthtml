package com.thewinx.identityaccess.api.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {

	@NotBlank
	private String username;

	@NotBlank
	private String password;

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
