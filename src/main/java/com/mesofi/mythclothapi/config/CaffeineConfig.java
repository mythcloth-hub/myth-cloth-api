package com.mesofi.mythclothapi.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CaffeineConfig {

    @Bean
    CacheManager cacheManager() {

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(
                Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofDays(15)).recordStats());

        return cacheManager;
    }
}
