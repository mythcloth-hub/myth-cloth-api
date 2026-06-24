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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mesofi.mythclothapi.security.JwtProperties;

/**
 * Configures application security using Spring Security with JWT-based authentication and
 * authorization.
 *
 * <p>This configuration defines the application's security model:
 *
 * <ul>
 *   <li>Enables Spring Security web protection and method-level security.
 *   <li>Configures the application as an OAuth2 Resource Server using JWT Bearer tokens.
 *   <li>Disables CSRF protection because the API is stateless and does not rely on server-side
 *       sessions.
 *   <li>Configures stateless session management.
 *   <li>Defines public and protected API endpoints.
 *   <li>Configures CORS support for the frontend application.
 *   <li>Maps JWT claims into Spring Security authorities.
 * </ul>
 *
 * <p>The JWT token is expected to contain authorization information through the following claims:
 *
 * <pre>{@code
 * {
 *   "roles": [
 *     "ADMIN"
 *   ],
 *   "permissions": [
 *     "figurines:read",
 *     "figurines:write"
 *   ]
 * }
 * }</pre>
 *
 * <p>Roles are converted into Spring Security roles using the {@code ROLE_} prefix, allowing
 * authorization rules such as:
 *
 * <pre>{@code
 * @PreAuthorize("hasRole('ADMIN')")
 * }</pre>
 *
 * <p>Permissions are mapped directly as authorities and can be used with:
 *
 * <pre>{@code
 * @PreAuthorize("hasAuthority('figurines:write')")
 * }</pre>
 *
 * <p>This configuration supports both URL-based authorization through the security filter chain and
 * method-level authorization through {@link EnableMethodSecurity}.
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
   *   <li>Disables CSRF protection for the stateless REST API.
   *   <li>Configures stateless session management.
   *   <li>Allows CORS requests from configured origins.
   *   <li>Allows unauthenticated access to public endpoints.
   *   <li>Requires authentication for all protected endpoints.
   *   <li>Enables JWT Bearer authentication through Spring OAuth2 Resource Server.
   * </ul>
   *
   * @param http the HTTP security builder used to configure application security
   * @return the configured security filter chain
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource())) // must be first
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(
                        GET, "/figurines/**", "/catalogs/{catalogType}/**", "/anniversaries/**")
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
   * Creates the CORS configuration used by the application.
   *
   * <p>This configuration allows requests from the configured frontend origin and enables the HTTP
   * methods required by the API.
   *
   * <p>The current configuration:
   *
   * <ul>
   *   <li>Allows requests from the local frontend development server.
   *   <li>Allows GET, POST, PUT, DELETE, and OPTIONS requests.
   *   <li>Allows all request headers.
   *   <li>Allows credentials to be included in requests.
   * </ul>
   *
   * @return the configured CORS source applied to all endpoints
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
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
