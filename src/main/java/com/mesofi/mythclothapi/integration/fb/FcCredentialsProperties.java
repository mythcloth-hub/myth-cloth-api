package com.mesofi.mythclothapi.integration.fb;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Facebook application credentials.
 *
 * @param appId Facebook application id
 * @param appSecret Facebook application secret
 */
@ConfigurationProperties(prefix = "myth-cloth.facebook")
public record FcCredentialsProperties(String appId, String appSecret, String graphUrl) {}
