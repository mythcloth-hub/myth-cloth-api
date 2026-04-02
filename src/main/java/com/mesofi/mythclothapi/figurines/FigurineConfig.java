package com.mesofi.mythclothapi.figurines;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers figurine-related Spring configuration, including binding for {@link
 * FigurineImportProperties} under the {@code myth-cloth.import} prefix.
 */
@Configuration
@EnableConfigurationProperties(FigurineImportProperties.class)
public class FigurineConfig {}
