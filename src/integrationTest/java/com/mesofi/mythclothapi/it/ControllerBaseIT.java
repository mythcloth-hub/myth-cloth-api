package com.mesofi.mythclothapi.it;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ControllerBaseIT {

  @LocalServerPort private int port;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  protected RestClient rest;

  @BeforeAll
  void setUpRestClient() {
    this.rest = RestClient.builder().baseUrl("http://localhost:" + port + contextPath).build();
  }
}
