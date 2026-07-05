package com.winx.booking.application;

import com.winx.booking.api.dto.PrincipalDto;
import com.winx.booking.api.dto.VehicleDto;
import com.winx.booking.exception.RestrictionViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class RestrictionValidator {

    public void validateAge(PrincipalDto principal, VehicleDto vehicle) {
        Integer minAge = vehicle.minAge();
        LocalDate dateOfBirth = principal.dateOfBirth();
        if (minAge == null || dateOfBirth == null) {
            return;
        }
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < minAge) {
            throw new RestrictionViolationException(
                    "User must be at least " + minAge + " to book this vehicle (age " + age + ").");
        }
    }
}
