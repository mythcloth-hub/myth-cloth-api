package com.mesofi.mythclothapi.demo;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the configuration properties used by the demo module.
 *
 * <p>
 * This configuration enables binding of properties under the
 * {@code myth-cloth.demo} prefix to {@link DemoProperties}, making them
 * available for dependency injection throughout the application.
 */
@Configuration
@EnableConfigurationProperties(DemoProperties.class)
public class DemoConfig {
}
