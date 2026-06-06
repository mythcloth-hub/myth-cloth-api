package com.mesofi.mythclothapi.integration.google;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.mesofi.mythclothapi.error.IntegrationException;
import com.mesofi.mythclothapi.integration.ServiceName;

import lombok.extern.slf4j.Slf4j;

/** Client responsible for validating Google ID tokens against Google's tokeninfo endpoint. */
@Slf4j
@Component
public class GoogleApiClient {

  private final RestClient restClient;

  /** Creates a Google API client configured with the OAuth base URL. */
  public GoogleApiClient() {
    this.restClient = RestClient.builder().baseUrl("https://oauth2.googleapis.com").build();
  }

  /**
   * Validates a Google ID token and returns its parsed claims.
   *
   * @param idToken Google ID token to validate
   * @return token info returned by Google
   * @throws IntegrationException if Google returns an error or empty response
   */
  public GoogleTokenInfoResponse validateIdToken(String idToken) {
    GoogleTokenInfoResponse response =
        applyErrorHandling(
                restClient
                    .get()
                    .uri(
                        uriBuilder ->
                            uriBuilder.path("/tokeninfo").queryParam("id_token", idToken).build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve())
            .body(GoogleTokenInfoResponse.class);

    if (response == null) {
      throw new IntegrationException(
          ServiceName.GOOGLE,
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Google returned an empty response.");
    }

    return response;
  }

  /**
   * Configures error handling for Google API responses.
   *
   * @param responseSpec response specification to decorate
   * @return response specification with mapped error handlers
   */
  private RestClient.ResponseSpec applyErrorHandling(RestClient.ResponseSpec responseSpec) {
    return responseSpec
        .onStatus(
            HttpStatusCode::is4xxClientError,
            (request, response) -> {
              log.error("Google API returned client error: {}", response.getStatusCode().value());
              throw new IntegrationException(
                  ServiceName.GOOGLE, response.getStatusCode().value(), "Google API client error.");
            })
        .onStatus(
            HttpStatusCode::is5xxServerError,
            (request, response) -> {
              log.error("Google API returned server error: {}", response.getStatusCode().value());
              throw new IntegrationException(
                  ServiceName.GOOGLE, response.getStatusCode().value(), "Google servers are down.");
            });
  }
}
