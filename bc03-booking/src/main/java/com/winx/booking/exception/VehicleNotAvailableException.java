package com.winx.booking.exception;

import org.springframework.http.HttpStatus;

public class VehicleNotAvailableException extends DomainException {
    public VehicleNotAvailableException(Long vehicleId) {
        super("Vehicle " + vehicleId + " is not available for booking.", HttpStatus.CONFLICT);
    }
}
