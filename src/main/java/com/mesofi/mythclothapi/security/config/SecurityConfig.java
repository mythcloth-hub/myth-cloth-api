package com.mesofi.mythclothapi.security.config;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.mesofi.mythclothapi.security.JwtProperties;

/**
 * Configures JWT-based authentication and authorization for the application.
 *
 * <p>This configuration:
 *
 * <ul>
 *   <li>Disables CSRF protection because the API is stateless.
 *   <li>Uses JWT Bearer tokens for authentication.
 *   <li>Does not create or use HTTP sessions.
 *   <li>Protects configured endpoints through Spring Security.
 *   <li>Converts JWT permission claims into Spring Security authorities.
 *   <li>Enables method-level authorization using {@code @PreAuthorize}.
 * </ul>
 *
 * <p>Incoming JWT tokens are validated using the configured {@link JwtDecoder}. The {@code
 * permissions} claim is mapped into {@link GrantedAuthority} instances, allowing endpoint access to
 * be controlled with expressions such as:
 *
 * <pre>{@code
 * @PreAuthorize("hasAuthority('catalogs:write')")
 * }</pre>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

  /**
   * Creates the application's security filter chain.
   *
   * <p>The configuration:
   *
   * <ul>
   *   <li>Disables CSRF protection.
   *   <li>Configures stateless session management.
   *   <li>Requires authentication for distributor endpoints.
   *   <li>Allows anonymous access to all other endpoints.
   *   <li>Enables JWT Bearer token authentication through Spring's OAuth2 Resource Server.
   * </ul>
   *
   * @param http the HTTP security builder
   * @return the configured security filter chain
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(GET, "/figurines/**")
                    .permitAll()
                    .requestMatchers(POST, "/collectors/auth/{provider}/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth ->
                oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  /**
   * Creates the JWT decoder responsible for validating incoming access tokens.
   *
   * <p>The decoder verifies:
   *
   * <ul>
   *   <li>Token signature.
   *   <li>Token expiration.
   *   <li>JWT structure and integrity.
   * </ul>
   *
   * <p>Validation is performed using the application's configured HMAC SHA-256 secret.
   *
   * @param properties security properties containing the JWT signing secret
   * @return a configured JWT decoder
   */
  @Bean
  JwtDecoder jwtDecoder(JwtProperties properties) {
    SecretKey key = new SecretKeySpec(properties.secret().getBytes(), "HmacSHA256");

    return NimbusJwtDecoder.withSecretKey(key).build();
  }

  /**
   * Creates a converter that transforms JWT permission and role claims into Spring Security
   * authorities.
   *
   * <p>Given a token containing:
   *
   * <pre>{@code
   * {
   *   "roles": [
   *     "ADMIN"
   *   ],
   *   "permissions": [
   *     "catalogs:read",
   *     "catalogs:write"
   *   ]
   * }
   * }</pre>
   *
   * <p>The converter produces the following authorities:
   *
   * <pre>{@code
   * new SimpleGrantedAuthority("ROLE_ADMIN")
   * new SimpleGrantedAuthority("catalogs:read")
   * new SimpleGrantedAuthority("catalogs:write")
   * }</pre>
   *
   * <p>Role values are prefixed with {@code ROLE_} to support Spring Security's {@code
   * hasRole(...)} expressions, while permission values are mapped directly to authorities for use
   * with {@code hasAuthority(...)} expressions.
   *
   * <p>The resulting authorities can be used by authorization rules such as:
   *
   * <pre>{@code
   * @PreAuthorize("hasRole('ADMIN')")
   * @PreAuthorize("hasAuthority('catalogs:write')")
   * @PreAuthorize(
   *     "hasRole('ADMIN') and hasAuthority('catalogs:write')")
   * }</pre>
   *
   * @return a JWT authentication converter that maps role and permission claims to Spring Security
   *     authorities
   */
  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    converter.setJwtGrantedAuthoritiesConverter(
        jwt -> {
          List<GrantedAuthority> authorities = new ArrayList<>();

          List<String> permissions = jwt.getClaimAsStringList("permissions");

          if (permissions != null) {
            authorities.addAll(permissions.stream().map(SimpleGrantedAuthority::new).toList());
          }

          List<String> roles = jwt.getClaimAsStringList("roles");

          if (roles != null) {
            authorities.addAll(
                roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList());
          }

          return authorities;
        });

    return converter;
  }
}
