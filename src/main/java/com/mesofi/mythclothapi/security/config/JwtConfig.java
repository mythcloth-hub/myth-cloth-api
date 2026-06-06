package com.mesofi.mythclothapi.security.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.mesofi.mythclothapi.security.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Spring configuration that exposes the JWT encoder used to sign tokens with the configured shared
 * secret.
 */
@Configuration
public class JwtConfig {
  @Bean
  JwtEncoder jwtEncoder(JwtProperties props) {
    SecretKey key =
        new SecretKeySpec(props.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }
}
