package com.mesofi.mythclothapi.figurines;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Registers figurine-related Spring configuration.
 */
@Configuration
public class FigurineConfig {

    /**
     * Creates the {@link TimedAspect} used by Spring AOP to intercept methods
     * annotated with {@link io.micrometer.core.annotation.Timed} and publish
     * execution time metrics to the configured {@link MeterRegistry}.
     *
     * @param registry
     *            the Micrometer meter registry used to record metrics
     * @return the configured {@link TimedAspect}
     */
    @Bean
    TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
