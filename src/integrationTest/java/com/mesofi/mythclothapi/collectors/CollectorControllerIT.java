package com.mesofi.mythclothapi.collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginResp;
import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.it.ControllerBaseIT;

@AutoConfigureMockMvc
public class CollectorControllerIT extends ControllerBaseIT {
  static WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());

  static {
    wireMockServer.start();
  }

  @AfterAll
  static void stop() {
    wireMockServer.stop();
  }

  @DynamicPropertySource
  static void configure(DynamicPropertyRegistry registry) {
    registry.add(
        "myth-cloth.facebook.graph-url", () -> "http://localhost:" + wireMockServer.port());
  }

  @Test
  @DisplayName("Test Facebook login flow and collector authentication")
  void fullFacebookLoginFlow() {

    final String fbFakeAccessToken = "fbFakeAccessToken";

    // Mock Facebook token validation
    wireMockServer.stubFor(
        get(urlPathEqualTo("/debug_token"))
            .withQueryParam("access_token", equalTo("0000|1111"))
            .withQueryParam("input_token", equalTo(fbFakeAccessToken))
            .willReturn(
                okJson(
                    """
                {
                  "data": {
                    "app_id": "0000",
                    "type": "USER",
                    "application": "MyApp",
                    "data_access_expires_at": 1789055926,
                    "expires_at": 1789055926,
                    "is_valid": true,
                    "scopes": [
                      "email", "public_profile"
                    ],
                    "user_id": "1234567890"
                  }
                }
            """)));

    // Mock Facebook user information
    wireMockServer.stubFor(
        get(urlPathEqualTo("/me"))
            .withQueryParam("access_token", equalTo(fbFakeAccessToken))
            .willReturn(
                okJson(
                    """
              {
                "id": "1234567890",
                "name": "Test User",
                "email": "test@test.com"
              }
              """)));

    ResponseEntity<CollectorLoginResp> collectorLoginResp =
        rest.post()
            .uri("/collectors/auth/{provider}", "facebook")
            .body(new CollectorLoginReq(null, fbFakeAccessToken))
            .retrieve()
            .toEntity(CollectorLoginResp.class);


    rest.post()
        .uri("/distributors")
        .body(new DistributorReq(DistributorName.BANDAI, CountryCode.JP, "www.google.com"))
        .retrieve()
        .toEntity(DistributorResp.class);
  }
}
