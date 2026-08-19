package com.mesofi.mythclothapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

/**
 * Web configuration for Spring Data pagination support.
 *
 * <p>
 * Configures Spring Data to serialize {@code Page} instances using a stable
 * DTO-based representation instead of serializing {@code PageImpl} instances
 * directly. This prevents pagination response structures from depending on the
 * internal implementation details of Spring Data.
 * </p>
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class WebConfig {
}
