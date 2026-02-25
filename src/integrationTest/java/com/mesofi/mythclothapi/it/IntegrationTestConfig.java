package com.mesofi.mythclothapi.it;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@TestConfiguration
public class IntegrationTestConfig {

  @Bean
  public RestClient restClient(RestClient.Builder builder, @LocalServerPort int port) {
    return builder.baseUrl("http://localhost:" + port).build();
  }
}
