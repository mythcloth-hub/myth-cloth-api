package com.mesofi.mythclothapi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "myth-cloth.security")
public record SecurityProperties(String corsUrl, JwtProperties jwt) {
}
