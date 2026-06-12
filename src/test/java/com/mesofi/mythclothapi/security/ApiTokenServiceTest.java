package com.mesofi.mythclothapi.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.security.roles.model.Role;

@ExtendWith(MockitoExtension.class)
class ApiTokenServiceTest {

  @InjectMocks private ApiTokenService service;

  @Mock private JwtEncoder jwtEncoder;
  @Mock private JwtProperties props;

  @Test
  void ttlSeconds_shouldReturnTtlMinutesInSeconds() {
    when(props.ttlMinutes()).thenReturn(15L);

    long result = service.ttlSeconds();

    assertThat(result).isEqualTo(900L);
  }

  @Test
  void generateToken_shouldBuildExpectedHeaderAndClaimsAndReturnTokenValue() {
    when(props.issuer()).thenReturn("myth-cloth-api");
    when(props.ttlMinutes()).thenReturn(30L);

    Jwt encodedJwt = org.mockito.Mockito.mock(Jwt.class);
    when(encodedJwt.getTokenValue()).thenReturn("signed-jwt-token");

    ArgumentCaptor<JwtEncoderParameters> captor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);
    when(jwtEncoder.encode(captor.capture())).thenReturn(encodedJwt);

    Role role = new Role();
    role.setDescription("Admin");

    Collector collector = new Collector();
    collector.setId(77L);
    collector.setDisplayName("Armando");
    collector.setRole(role);

    String token = service.generateToken(collector, "GOOGLE", "sub-123", "seiya@example.com");

    assertThat(token).isEqualTo("signed-jwt-token");

    JwtEncoderParameters params = captor.getValue();
    JwtClaimsSet claims = params.getClaims();

    assertThat(params.getJwsHeader().getAlgorithm()).isEqualTo(MacAlgorithm.HS256);
    assertThat(claims.getClaims().get("iss")).isEqualTo("myth-cloth-api");
    assertThat(claims.getClaims().get("sub")).isEqualTo("77");
    assertThat(claims.getClaimAsString("provider")).isEqualTo("GOOGLE");
    assertThat(claims.getClaimAsString("provider_user_id")).isEqualTo("sub-123");
    assertThat(claims.getClaimAsString("email")).isEqualTo("seiya@example.com");
    assertThat(claims.getIssuedAt()).isNotNull();
    assertThat(claims.getExpiresAt()).isNotNull();
    assertThat(Duration.between(claims.getIssuedAt(), claims.getExpiresAt()).getSeconds())
        .isEqualTo(1800L);

    verify(jwtEncoder).encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class));
  }
}
