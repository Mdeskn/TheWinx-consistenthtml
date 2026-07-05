package com.winx.booking.api.error;

import java.time.LocalDateTime;

/** Standard error body shared by all REST endpoints  */
public record ErrorResponse(
        String error,
        String message,
        int status,
        String path,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String error, String message, int status, String path) {
        return new ErrorResponse(error, message, status, path, LocalDateTime.now());
    }
}
