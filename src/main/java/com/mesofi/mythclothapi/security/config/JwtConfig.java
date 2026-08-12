package com.mesofi.mythclothapi.security.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Spring configuration for JWT encoding.
 *
 * <p>
 * Exposes a {@link JwtEncoder} that signs JWTs using the HMAC-SHA256 algorithm
 * and the shared secret configured through {@link SecurityProperties}.
 * </p>
 */
@Configuration
public class JwtConfig {

    /**
     * Creates the JWT encoder used to sign application tokens.
     *
     * <p>
     * The configured JWT secret is converted to a UTF-8 encoded key and used with
     * the HMAC-SHA256 algorithm.
     * </p>
     *
     * @param security
     *            the application's security configuration properties
     * @return a configured {@link JwtEncoder} for signing JWTs
     */
    @Bean
    JwtEncoder jwtEncoder(SecurityProperties security) {
        SecretKey key = new SecretKeySpec(security.jwt().secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }
}