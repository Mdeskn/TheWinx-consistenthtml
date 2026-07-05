package com.thewinx.identityaccess.api.dto;

import jakarta.validation.constraints.NotBlank;

public class RoleAssignmentRequest {

	@NotBlank
	private String roleName;

	public String getRoleName() {
		return roleName;
	}
}
