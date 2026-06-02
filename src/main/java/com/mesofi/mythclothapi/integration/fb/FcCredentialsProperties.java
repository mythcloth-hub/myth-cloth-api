package com.mesofi.mythclothapi.integration.fb;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "myth-cloth.facebook")
public record FcCredentialsProperties(String appId, String appSecret) {}
