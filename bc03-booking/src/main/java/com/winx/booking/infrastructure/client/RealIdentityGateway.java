package com.winx.booking.infrastructure.client;

import com.winx.booking.api.dto.PrincipalDto;
import com.winx.booking.exception.DependencyUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!mock")
@RequiredArgsConstructor
public class RealIdentityGateway implements IdentityGateway {

    private final IdentityClient client;

    @Override
    @CircuitBreaker(name = "identity", fallbackMethod = "validateFallback")
    public PrincipalDto validate(String token) {
        return client.validate(token);
    }

    @SuppressWarnings("unused")
    private PrincipalDto validateFallback(String token, Throwable t) {
        throw new DependencyUnavailableException("Identity & Access unavailable", t);
    }
}
