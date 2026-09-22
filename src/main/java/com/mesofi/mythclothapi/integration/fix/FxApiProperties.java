package com.mesofi.mythclothapi.integration.fix;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FxAPI integration.
 *
 * @param url
 *            base URL for FxAPI's endpoint
 */
@ConfigurationProperties(prefix = "myth-cloth.fxapi")
public record FxApiProperties(String url) {
}
