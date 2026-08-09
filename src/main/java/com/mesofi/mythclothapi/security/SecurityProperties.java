package com.mesofi.mythclothapi.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "myth-cloth.security")
public record SecurityProperties(List<String> corsUrls, JwtProperties jwt) {
}
