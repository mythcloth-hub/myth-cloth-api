package com.mesofi.mythclothapi.integration.google;

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

class GoogleApiClientTest {

  private TestContext context() {
    GoogleApiClient client = new GoogleApiClient();
    RestClient.Builder restClientBuilder =
        RestClient.builder().baseUrl("https://oauth2.googleapis.com");
    MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();

    ReflectionTestUtils.setField(client, "restClient", restClientBuilder.build());
    return new TestContext(client, server);
  }

  private record TestContext(GoogleApiClient client, MockRestServiceServer server) {}

  @Test
  void validateIdToken_shouldReturnTokenInfo_whenPayloadIsPresent() {
    TestContext context = context();

    context
        .server()
        .expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=google-id-token"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                """
                {
                  "iss": "https://accounts.google.com",
                  "aud": "client-id",
                  "sub": "sub-123",
                  "email": "seiya@example.com",
                  "email_verified": "true",
                  "name": "Seiya",
                  "picture": "https://img/seiya.jpg",
                  "exp": "1735689600"
                }
                """,
                APPLICATION_JSON));

    GoogleTokenInfoResponse response = context.client().validateIdToken("google-id-token");

    context.server().verify();

    assertThat(response.iss()).isEqualTo("https://accounts.google.com");
    assertThat(response.aud()).isEqualTo("client-id");
    assertThat(response.sub()).isEqualTo("sub-123");
    assertThat(response.email()).isEqualTo("seiya@example.com");
    assertThat(response.emailVerified()).isTrue();
    assertThat(response.name()).isEqualTo("Seiya");
    assertThat(response.picture()).isEqualTo("https://img/seiya.jpg");
    assertThat(response.exp()).isEqualTo("1735689600");
  }

  @Test
  void validateIdToken_shouldThrowIntegrationException_whenGoogleReturnsClientError() {
    TestContext context = context();

    context
        .server()
        .expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=google-id-token"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST));

    assertThatThrownBy(() -> context.client().validateIdToken("google-id-token"))
        .isInstanceOfSatisfying(
            IntegrationException.class,
            ex -> {
              assertThat(ex.getServiceName()).isEqualTo(ServiceName.GOOGLE);
              assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(ex.getMessage()).isEqualTo("Google API client error.");
            });

    context.server().verify();
  }

  @Test
  void validateIdToken_shouldThrowIntegrationException_whenGoogleReturnsServerError() {
    TestContext context = context();

    context
        .server()
        .expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=google-id-token"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

    assertThatThrownBy(() -> context.client().validateIdToken("google-id-token"))
        .isInstanceOfSatisfying(
            IntegrationException.class,
            ex -> {
              assertThat(ex.getServiceName()).isEqualTo(ServiceName.GOOGLE);
              assertThat(ex.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
              assertThat(ex.getMessage()).isEqualTo("Google servers are down.");
            });

    context.server().verify();
  }

  @Test
  void validateIdToken_shouldThrowIntegrationException_whenGoogleReturnsEmptyBody() {
    TestContext context = context();

    context
        .server()
        .expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=google-id-token"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withNoContent());

    assertThatThrownBy(() -> context.client().validateIdToken("google-id-token"))
        .isInstanceOfSatisfying(
            IntegrationException.class,
            ex -> {
              assertThat(ex.getServiceName()).isEqualTo(ServiceName.GOOGLE);
              assertThat(ex.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
              assertThat(ex.getMessage()).isEqualTo("Google returned an empty response.");
            });

    context.server().verify();
  }
}
