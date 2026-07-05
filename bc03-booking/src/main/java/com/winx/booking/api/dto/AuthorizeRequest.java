package com.winx.booking.api.dto;

import java.math.BigDecimal;

public record AuthorizeRequest(
        Long bookingId,
        Long userId,
        BigDecimal amount,
        String currency,
        String paymentMethod
) {
}
