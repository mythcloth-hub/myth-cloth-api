package com.mesofi.mythclothapi.figurines;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Registers figurine-related Spring configuration, including binding for
 * {@link FigurineImportProperties} under the {@code myth-cloth.import} prefix.
 */
@Configuration
@EnableConfigurationProperties(FigurineImportProperties.class)
public class FigurineConfig {

    @Bean
    TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
