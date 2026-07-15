package com.mesofi.mythclothapi.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Configures the in-memory cache manager used by Spring cache annotations.
 *
 * <p>
 * The cache is bounded and time-based, with statistics enabled for actuator
 * exposure and cache monitoring.
 */
@Configuration
public class CaffeineConfig {

    /**
     * Creates the application's {@link CacheManager}.
     *
     * @return a Caffeine-backed cache manager with bounded size, 15-day expiry, and
     *         statistics enabled
     */
    @Bean
    CacheManager cacheManager() {

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(
                Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofDays(15)).recordStats());

        return cacheManager;
    }
}
