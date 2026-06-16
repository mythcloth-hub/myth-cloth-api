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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginResp;
import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.it.ControllerBaseIT;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionReq;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;
import com.mesofi.mythclothapi.security.rolepermissions.dto.RolePermissionReq;
import com.mesofi.mythclothapi.security.roles.dto.RoleReq;
import com.mesofi.mythclothapi.security.roles.dto.RoleResp;

@AutoConfigureMockMvc
@Sql(scripts = "/cleanup-collector-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
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
                                "name": "Facebook Testing User",
                                "email": "test@facebook.com"
                              }
                              """)));

    // Prepare the setup for this scenario.

    PermissionResp distributorWritePermission =
        rest.post()
            .uri("/permissions")
            .body(new PermissionReq("distributors:write"))
            .retrieve()
            .body(PermissionResp.class);

    PermissionResp distributorReadPermission =
        rest.post()
            .uri("/permissions")
            .body(new PermissionReq("distributors:read"))
            .retrieve()
            .body(PermissionResp.class);

    RoleResp adminRole =
        rest.post().uri("/roles").body(new RoleReq("Admin")).retrieve().body(RoleResp.class);

    assert adminRole != null;
    assert distributorWritePermission != null;
    assert distributorReadPermission != null;

    assignPermissionToRole(adminRole.id(), distributorWritePermission.id());
    assignPermissionToRole(adminRole.id(), distributorReadPermission.id());

    // The user logs in using Facebook and receives a JWT token from our system. This token should
    // have the permissions assigned to the Admin role.

    CollectorLoginResp collectorLoginResp =
        rest.post()
            .uri("/collectors/auth/{provider}", "facebook")
            .body(new CollectorLoginReq(null, fbFakeAccessToken))
            .retrieve()
            .body(CollectorLoginResp.class);

    assert collectorLoginResp != null;

    // Call the protected resource using the collector's JWT token and verify access is granted due
    // to the Admin role permissions.
    rest.post()
        .uri("/distributors")
        .headers(httpHeaders -> httpHeaders.setBearerAuth(collectorLoginResp.accessToken()))
        .body(new DistributorReq(DistributorName.BANDAI, CountryCode.JP, "www.google.com"))
        .retrieve()
        .toEntity(DistributorResp.class);
  }

  private void assignPermissionToRole(long roleId, long permissionId) {
    rest.post()
        .uri("/roles/{roleId}/permissions", roleId)
        .body(new RolePermissionReq(permissionId))
        .retrieve()
        .toBodilessEntity();
  }
}
