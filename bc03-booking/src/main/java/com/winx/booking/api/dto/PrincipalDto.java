package com.winx.booking.api.dto;

import java.time.LocalDate;

public record PrincipalDto(
        Long id,
        String kind,
        String email,
        LocalDate dateOfBirth
) {
}
