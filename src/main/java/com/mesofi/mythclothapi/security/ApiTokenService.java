package com.mesofi.mythclothapi.security;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/** Service responsible for generating signed API tokens for authenticated collectors. */
@Service
@RequiredArgsConstructor
public class ApiTokenService {

  private final JwtEncoder jwtEncoder;
  private final JwtProperties props;

  /**
   * Generates a JWT containing collector and provider identity details.
   *
   * @param collectorId collector identifier used as the token subject
   * @param provider authentication provider name
   * @param providerUserId provider-specific user identifier
   * @param email collector email address
   * @return signed JWT token value
   */
  public String generateToken(
      Long collectorId, String provider, String providerUserId, String email) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(ttlSeconds());

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(props.issuer())
            .issuedAt(now)
            .expiresAt(exp)
            .subject(String.valueOf(collectorId))
            .claim("provider", provider)
            .claim("provider_user_id", providerUserId)
            .claim("email", email)
            .build();

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }

  /** Returns the configured token time-to-live in seconds. */
  public long ttlSeconds() {
    return props.ttlMinutes() * 60;
  }
}
