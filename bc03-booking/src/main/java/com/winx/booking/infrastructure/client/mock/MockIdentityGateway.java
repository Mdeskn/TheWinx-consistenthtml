package com.winx.booking.infrastructure.client.mock;

import com.winx.booking.api.dto.PrincipalDto;
import com.winx.booking.infrastructure.client.IdentityGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Profile("mock")
public class MockIdentityGateway implements IdentityGateway {

    @Override
    public PrincipalDto validate(String token) {
        return new PrincipalDto(1L, "USER", "demo.user@winx.dev", LocalDate.of(1995, 5, 20));
    }
}
