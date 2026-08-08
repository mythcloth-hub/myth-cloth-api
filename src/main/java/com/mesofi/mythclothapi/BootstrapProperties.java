package com.mesofi.mythclothapi;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.mesofi.mythclothapi.collectorproviders.model.ProviderType;

@ConfigurationProperties(prefix = "myth-cloth.bootstrap")
public record BootstrapProperties(Map<ProviderType, String> admin) {

}
