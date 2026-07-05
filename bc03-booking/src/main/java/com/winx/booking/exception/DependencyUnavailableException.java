package com.winx.booking.exception;

import org.springframework.http.HttpStatus;

public class DependencyUnavailableException extends DomainException {
    public DependencyUnavailableException(String message, Throwable cause) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, cause);
    }
}
