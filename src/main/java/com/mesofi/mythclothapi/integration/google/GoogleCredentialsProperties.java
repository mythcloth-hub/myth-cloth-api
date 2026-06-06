package com.mesofi.mythclothapi.integration.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Google OAuth integration credentials.
 *
 * @param clientId Google OAuth client id expected in token audience claims
 */
@ConfigurationProperties(prefix = "myth-cloth.google")
public record GoogleCredentialsProperties(String clientId) {}
