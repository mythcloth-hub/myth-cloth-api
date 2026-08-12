package com.mesofi.mythclothapi.security.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.security.config.SecurityProperties;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.rolepermissions.RolePermission;

import lombok.RequiredArgsConstructor;

/**
 * Service responsible for generating signed API tokens for authenticated
 * collectors.
 */
@Service
@RequiredArgsConstructor
public class ApiTokenService {

    private final JwtEncoder jwtEncoder;
    private final SecurityProperties security;

    /**
     * Generates a JWT containing collector and provider identity details.
     *
     * @param collector
     *            collector used as the token subject
     * @param provider
     *            authentication provider name
     * @param providerUserId
     *            provider-specific user identifier
     * @param email
     *            collector email address
     * @return signed JWT token value
     */
    public String generateToken(Collector collector, String provider, String providerUserId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds());

        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(security.jwt().issuer()).issuedAt(now).expiresAt(exp)
                .subject(String.valueOf(collector.getId())) // database ID in sub
                .claim("email", email).claim("name", collector.getDisplayName())
                .claim("roles", List.of(collector.getRole().getName().toUpperCase()))
                .claim("permissions",
                        collector.getRole().getPermissions().stream().map(RolePermission::getPermission)
                                .map(Permission::getName).toList())
                .claim("provider", provider).claim("provider_user_id", providerUserId)
                .claim("jti", UUID.randomUUID().toString()).build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /** Returns the configured token time-to-live in seconds. */
    public long ttlSeconds() {
        return security.jwt().ttlMinutes() * 60;
    }
}
