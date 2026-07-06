package com.winx.booking.infrastructure.client;

import com.winx.booking.api.dto.AuthorizeRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!mock")
@RequiredArgsConstructor
@Slf4j
public class RealPaymentGateway implements PaymentGateway {

    private final PaymentClient client;

    @Override
    @CircuitBreaker(name = "payment", fallbackMethod = "paymentDeferred")
    public void authorize(AuthorizeRequest request) {
        client.authorize(request);
    }

    @Override
    @CircuitBreaker(name = "payment", fallbackMethod = "cancelDeferred")
    public void cancelPayment(Long bookingId) {
        client.cancelPayment(bookingId);
    }

    @SuppressWarnings("unused")
    private void paymentDeferred(AuthorizeRequest request, Throwable t) {
        log.warn("Payment unavailable; deferring payment for booking {} ({} {}). Reason: {}",
                request.bookingId(), request.amount(), request.currency(), t.toString());
    }

    @SuppressWarnings("unused")
    private void cancelDeferred(Long bookingId, Throwable t) {
        log.warn("Payment cancel unavailable for booking {}; no payment record may exist yet. Reason: {}",
                bookingId, t.getMessage());
    }
}
