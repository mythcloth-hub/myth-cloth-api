package com.mesofi.mythclothapi.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables application-wide Spring caching support.
 *
 * <p>
 * This activates Spring's cache interception so methods annotated with
 * {@code @Cacheable}, {@code @CacheEvict}, or {@code @CachePut} can use the
 * configured cache manager.
 */
@Configuration
@EnableCaching
public class MythCollectionConfig {
}
