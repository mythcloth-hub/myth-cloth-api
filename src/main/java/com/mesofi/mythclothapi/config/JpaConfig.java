package com.mesofi.mythclothapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration for Spring Data JPA auditing.
 * <p>
 * Enables automatic management of entity auditing fields such as creation and
 * last modification timestamps through {@code @CreatedDate} and
 * {@code @LastModifiedDate}.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
