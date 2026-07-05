package com.winx.booking.exception;

import org.springframework.http.HttpStatus;

public class ActiveBookingExistsException extends DomainException {
    public ActiveBookingExistsException(Long userId) {
        super("User " + userId + " already has an active booking.", HttpStatus.CONFLICT);
    }
}
