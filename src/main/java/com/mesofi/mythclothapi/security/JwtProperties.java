package com.mesofi.mythclothapi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT-based security settings.
 *
 * @param secret the signing secret used to generate and validate tokens
 * @param issuer the expected token issuer
 * @param ttlMinutes the token time-to-live in minutes
 */
@ConfigurationProperties(prefix = "myth-cloth.security.jwt")
public record JwtProperties(String secret, String issuer, long ttlMinutes) {}
