package com.mesofi.mythclothapi.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for demo-related features.
 *
 * <p>
 * These properties control optional functionality that is only intended for
 * demo or development environments.
 */
@ConfigurationProperties(prefix = "myth-cloth.demo")
public record DemoProperties(Boolean enabled, String providerUserId, String name, String email) {
}
