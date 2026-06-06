package com.mesofi.mythclothapi.collectors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.mesofi.mythclothapi.integration.fb.FcCredentialsProperties;
import com.mesofi.mythclothapi.integration.google.GoogleCredentialsProperties;

/** Registers provider credential properties required by collector social-auth services. */
@Configuration
@EnableConfigurationProperties({FcCredentialsProperties.class, GoogleCredentialsProperties.class})
public class CollectorConfig {}
