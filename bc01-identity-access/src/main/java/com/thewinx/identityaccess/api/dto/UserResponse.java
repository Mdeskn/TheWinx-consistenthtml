package com.thewinx.identityaccess.api.dto;

import com.thewinx.identityaccess.domain.AccountStatus;
import com.thewinx.identityaccess.domain.UserAccount;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public class UserResponse {

    private final Long id;
    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final LocalDate dateOfBirth;
    private final AccountStatus status;
    private final Set<String> roles;

    public UserResponse(Long id, String username, String email, String firstName, String lastName,
                        String phoneNumber, LocalDate dateOfBirth, AccountStatus status, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
        this.roles = roles;
    }

    public static UserResponse from(UserAccount user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getDateOfBirth(),
            user.getStatus(),
            user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet())
        );
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
