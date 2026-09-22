package com.mesofi.mythclothapi.collectors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.mesofi.mythclothapi.BootstrapProperties;
import com.mesofi.mythclothapi.integration.fb.FcCredentialsProperties;
import com.mesofi.mythclothapi.integration.fix.FxApiProperties;
import com.mesofi.mythclothapi.integration.google.GoogleCredentialsProperties;

/**
 * Spring configuration for the collector module.
 *
 * <p>
 * Registers the configuration properties required by the collector module,
 * including external service credentials, foreign exchange API settings, and
 * application bootstrap settings.
 * </p>
 */
@Configuration
@EnableConfigurationProperties({FcCredentialsProperties.class, GoogleCredentialsProperties.class, FxApiProperties.class,
        BootstrapProperties.class})
public class CollectorConfig {
}
