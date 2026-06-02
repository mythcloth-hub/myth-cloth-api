package com.mesofi.mythclothapi.collectors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.mesofi.mythclothapi.integration.fb.FcCredentialsProperties;

@Configuration
@EnableConfigurationProperties(FcCredentialsProperties.class)
public class CollectorConfig {}
