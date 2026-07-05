package com.winx.booking.domain.event;

import java.math.BigDecimal;

public record BookingCompleted(Long bookingId, Long userId, BigDecimal amount, String currency) {
}
