package com.mesofi.mythclothapi.security.config;

/**
 * Configuration properties for JWT-based security settings.
 *
 * @param secret
 *            the signing secret used to generate and validate tokens
 * @param issuer
 *            the expected token issuer
 * @param ttlMinutes
 *            the token time-to-live in minutes
 */
public record JwtProperties(String secret, String issuer, long ttlMinutes) {
}
