package com.winx.booking.infrastructure.client;

import com.winx.booking.api.dto.AuthorizeRequest;

public interface PaymentGateway {

    void authorize(AuthorizeRequest request);

    void cancelPayment(Long bookingId);
}
