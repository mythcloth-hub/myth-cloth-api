package com.mesofi.mythclothapi.security;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiTokenService {

  private final JwtEncoder jwtEncoder;
  private final JwtProperties props;

  public String generateToken(
      Long collectorId, String provider, String providerUserId, String email) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.ttlMinutes() * 60);

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
}
