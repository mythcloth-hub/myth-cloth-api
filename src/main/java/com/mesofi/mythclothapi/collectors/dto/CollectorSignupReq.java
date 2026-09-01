package com.mesofi.mythclothapi.collectors.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Signup request payload containing collector details.
 *
 * @param fullName
 *            full name of the collector
 * @param email
 *            email address of the collector
 * @param password
 *            password for the collector account. Must be 8 to 64 characters and
 *            include at least one uppercase letter, one lowercase letter, one
 *            number, and one special character
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorSignupReq(
        @NotBlank(message = "fullName is required") @Size(min = 2, max = 100, message = "fullName must be between 2 and 100 characters") String fullName,
        @NotBlank(message = "email is required") @Email(message = "email is invalid") String email,
        @NotBlank(message = "password is required") @Size(min = 8, max = 64, message = "password must be between 8 and 64 characters") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).+$", message = "password must include at least one uppercase letter, one lowercase letter, one number, and one special character") String password) {
}
