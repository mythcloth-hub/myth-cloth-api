package com.mesofi.mythclothapi.integration.fb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.mesofi.mythclothapi.error.IntegrationException;
import com.mesofi.mythclothapi.integration.ServiceName;

class FbApiClientTest {

  private TestContext context() {
    FbApiClient client =
        new FbApiClient(
            new FcCredentialsProperties("app-id", "app-secret", "https://graph.facebook.com/"));
    RestClient.Builder restClientBuilder =
        RestClient.builder().baseUrl("https://graph.facebook.com/");
    MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();

    ReflectionTestUtils.setField(client, "restClient", restClientBuilder.build());
    return new TestContext(client, server);
  }

  private record TestContext(FbApiClient client, MockRestServiceServer server) {}

  @Test
  void getUserInfo_shouldReturnProfileWhenPayloadIsPresent() {
    TestContext context = context();

    context
        .server()
        .expect(
            requestTo(
                "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=user-token"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                """
                {"id":"123","name":"Seiya","email":"seiya@example.com"}
                """,
                APPLICATION_JSON));

    FbUserInfoResponse response = context.client().getUserInfo("user-token");

    context.server().verify();

    assertThat(response.id()).isEqualTo("123");
    assertThat(response.name()).isEqualTo("Seiya");
    assertThat(response.email()).isEqualTo("seiya@example.com");
  }

  @Test
  void validateAccessToken_shouldReturnTokenDetailsWhenPayloadIsPresent() {
    TestContext context = context();

    context
        .server()
        .expect(
            requestTo(
                "https://graph.facebook.com/debug_token?access_token=app-id%7Capp-secret&input_token=user-token"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                """
                {
                  "data": {
                    "app_id": "app-id",
                    "type": "USER",
                    "application": "Myth Cloth",
                    "data_access_expires_at": 1111,
                    "expires_at": 2222,
                    "is_valid": true,
                    "scopes": ["email", "public_profile"],
                    "user_id": "123"
                  }
                }
                """,
                APPLICATION_JSON));

    FbTokenResponse response = context.client().validateAccessToken("user-token");

    context.server().verify();

    assertThat(response.data().appId()).isEqualTo("app-id");
    assertThat(response.data().userId()).isEqualTo("123");
    assertThat(response.data().valid()).isTrue();
  }

  @Test
  void getUserInfo_shouldThrowIntegrationException_whenFacebookReturnsNotFound() {
    TestContext context = context();

    context
        .server()
        .expect(
            requestTo(
                "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=user-token"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    assertThatThrownBy(() -> context.client().getUserInfo("user-token"))
        .isInstanceOfSatisfying(
            IntegrationException.class,
            ex -> {
              assertThat(ex.getServiceName()).isEqualTo(ServiceName.FACEBOOK);
              assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
              assertThat(ex.getMessage()).isEqualTo("Facebook resource not found.");
            });

    context.server().verify();
  }

  @Test
  void getUserInfo_shouldThrowIntegrationException_whenFacebookReturnsServerError() {
    TestContext context = context();

    context
        .server()
        .expect(
            requestTo(
                "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=user-token"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

    assertThatThrownBy(() -> context.client().getUserInfo("user-token"))
        .isInstanceOfSatisfying(
            IntegrationException.class,
            ex -> {
              assertThat(ex.getServiceName()).isEqualTo(ServiceName.FACEBOOK);
              assertThat(ex.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
              assertThat(ex.getMessage()).isEqualTo("Facebook servers are down.");
            });

    context.server().verify();
  }

  @Test
  void validateAccessToken_shouldThrowIntegrationException_whenFacebookReturnsGenericClientError() {
    TestContext context = context();

    context
        .server()
        .expect(
            requestTo(
                "https://graph.facebook.com/debug_token?access_token=app-id%7Capp-secret&input_token=user-token"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST));

    assertThatThrownBy(() -> context.client().validateAccessToken("user-token"))
        .isInstanceOfSatisfying(
            IntegrationException.class,
            ex -> {
              assertThat(ex.getServiceName()).isEqualTo(ServiceName.FACEBOOK);
              assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(ex.getMessage()).isEqualTo("Facebook API client error.");
            });

    context.server().verify();
  }

  @Test
  void getUserInfo_shouldThrowIntegrationException_whenFacebookReturnsEmptyBody() {
    TestContext context = context();

    context
        .server()
        .expect(
            requestTo(
                "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=user-token"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withNoContent());

    assertThatThrownBy(() -> context.client().getUserInfo("user-token"))
        .isInstanceOfSatisfying(
            IntegrationException.class,
            ex -> {
              assertThat(ex.getServiceName()).isEqualTo(ServiceName.FACEBOOK);
              assertThat(ex.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
              assertThat(ex.getMessage()).isEqualTo("Facebook returned an empty response.");
            });

    context.server().verify();
  }
}
