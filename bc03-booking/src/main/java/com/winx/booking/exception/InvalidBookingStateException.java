package com.winx.booking.exception;

import org.springframework.http.HttpStatus;

public class InvalidBookingStateException extends DomainException {
    public InvalidBookingStateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
