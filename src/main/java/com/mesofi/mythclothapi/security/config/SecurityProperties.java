package com.mesofi.mythclothapi.security.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the application's security settings.
 *
 * <p>
 * Properties are loaded from the {@code myth-cloth.security} configuration
 * prefix and include the allowed CORS origins and JWT configuration.
 * </p>
 *
 * @param corsUrls
 *            the list of origins allowed to make cross-origin requests
 * @param jwt
 *            the configuration properties used for JWT authentication
 */
@ConfigurationProperties(prefix = "myth-cloth.security")
public record SecurityProperties(List<String> corsUrls, JwtProperties jwt) {
}