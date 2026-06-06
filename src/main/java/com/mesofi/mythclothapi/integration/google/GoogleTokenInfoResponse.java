package com.mesofi.mythclothapi.integration.google;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Google token introspection response payload for ID token validation.
 *
 * @param iss token issuer
 * @param aud token audience (client id)
 * @param sub Google user subject identifier
 * @param email user email
 * @param emailVerifiedRaw raw email verification claim from Google
 * @param name user display name
 * @param picture user profile picture URL
 * @param exp token expiration epoch-seconds represented as a string
 */
public record GoogleTokenInfoResponse(
    String iss,
    String aud,
    String sub,
    String email,
    @JsonProperty("email_verified") String emailVerifiedRaw,
    String name,
    String picture,
    String exp) {

  /**
   * Indicates whether Google reported the email claim as verified.
   *
   * @return true when email verification claim is true
   */
  public boolean emailVerified() {
    return Boolean.parseBoolean(emailVerifiedRaw);
  }

  /**
   * Parses the token expiration time claim as epoch seconds.
   *
   * @return expiration time in epoch seconds
   * @throws NumberFormatException if {@code exp} is not numeric
   */
  public long expiresAtEpochSecond() {
    return Long.parseLong(exp);
  }
}
