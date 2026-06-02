package com.mesofi.mythclothapi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "myth-cloth.security.jwt")
public record JwtProperties(String secret, String issuer, long ttlMinutes) {}
