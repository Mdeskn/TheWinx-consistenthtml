package com.winx.booking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class DomainException extends RuntimeException {

    private final HttpStatus status;

    protected DomainException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    protected DomainException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
