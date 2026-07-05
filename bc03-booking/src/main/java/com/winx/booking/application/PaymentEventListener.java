package com.winx.booking.application;

import com.winx.booking.api.dto.AuthorizeRequest;
import com.winx.booking.domain.event.BookingCompleted;
import com.winx.booking.infrastructure.client.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private static final String DEFAULT_PAYMENT_METHOD = "CARD";

    private final PaymentGateway paymentGateway;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCompleted(BookingCompleted event) {
        paymentGateway.authorize(new AuthorizeRequest(
                event.bookingId(),
                event.userId(),
                event.amount(),
                event.currency(),
                DEFAULT_PAYMENT_METHOD));
    }
}
