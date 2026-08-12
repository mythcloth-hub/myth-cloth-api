package com.mesofi.mythclothapi.figurineimports;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the figurine import feature.
 *
 * <p>
 * Enables {@link FigurineImportProperties} so that figurine import settings
 * defined in the application configuration can be injected into the import
 * components.
 * </p>
 */
@Configuration
@EnableConfigurationProperties(FigurineImportProperties.class)
public class FigurineImportConfig {
}
