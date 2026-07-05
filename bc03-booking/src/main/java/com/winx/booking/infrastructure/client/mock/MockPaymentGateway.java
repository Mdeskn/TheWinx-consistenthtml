package com.winx.booking.infrastructure.client.mock;

import com.winx.booking.api.dto.AuthorizeRequest;
import com.winx.booking.infrastructure.client.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mock")
@Slf4j
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public void authorize(AuthorizeRequest request) {
        log.info("[mock] Payment authorized for booking {} amount {} {}",
                request.bookingId(), request.amount(), request.currency());
    }
}
