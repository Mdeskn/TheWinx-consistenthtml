package com.winx.booking.infrastructure.client;

import com.winx.booking.api.dto.PrincipalDto;

public interface IdentityGateway {

    PrincipalDto validate(String token);
}
