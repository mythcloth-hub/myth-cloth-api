package com.mesofi.mythclothapi.collectors.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Login request payload containing provider-issued authentication tokens.
 *
 * @param idToken
 *            OpenID Connect ID token (used by providers such as Google)
 * @param accessToken
 *            OAuth access token (used by providers such as Facebook).
 * @param email
 *            collector email address. Optional, but when provided must be a
 *            valid email format
 * @param password
 *            collector password. Optional, but when provided must be 8 to 64
 *            characters and include at least one uppercase letter, one
 *            lowercase letter, one number, and one special character
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CollectorLoginReq(String idToken, String accessToken, @Email(message = "email is invalid") String email,
        @Size(min = 8, max = 64, message = "password must be between 8 and 64 characters") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).+$", message = "password must include at least one uppercase letter, one lowercase letter, one number, and one special character") String password) {
}
