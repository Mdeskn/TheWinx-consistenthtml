package com.winx.booking.exception;

import org.springframework.http.HttpStatus;

public class RestrictionViolationException extends DomainException {
    public RestrictionViolationException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
