package com.mesofi.mythclothapi.integration.fb;

import static com.mesofi.mythclothapi.integration.ServiceName.FACEBOOK;

import java.net.URI;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import com.mesofi.mythclothapi.error.IntegrationException;

import lombok.extern.slf4j.Slf4j;

/**
 * Client responsible for interacting with the Facebook Graph API.
 *
 * <p>This client provides operations for:
 *
 * <ul>
 *   <li>Retrieving Facebook user profile information.
 *   <li>Validating Facebook access tokens.
 * </ul>
 *
 * <p>All communication errors are translated into {@link IntegrationException} instances to provide
 * a consistent integration error model across the application.
 */
@Slf4j
@Component
public class FbApiClient {
  private final RestClient restClient;
  private final FcCredentialsProperties fcCredentials;

  /**
   * Creates a new Facebook API client.
   *
   * @param fcCredentials Facebook application credentials used for token validation.
   */
  public FbApiClient(FcCredentialsProperties fcCredentials) {
    this.restClient = RestClient.builder().baseUrl("https://graph.facebook.com/").build();
    this.fcCredentials = fcCredentials;
  }

  /**
   * Retrieves profile information for the user associated with the provided Facebook access token.
   *
   * @param accessToken Facebook user access token.
   * @return the Facebook user profile information.
   * @throws IntegrationException if the Facebook API returns an error or an invalid response.
   */
  public FbUserInfoResponse getUserInfo(String accessToken) {
    return getFbResponse(
        uriBuilder ->
            uriBuilder
                .path("/me")
                .queryParam("fields", "id,name,email,picture")
                .queryParam("access_token", accessToken)
                .build(),
        FbUserInfoResponse.class);
  }

  /**
   * Validates a Facebook access token using the Facebook Debug Token API.
   *
   * <p>The validation is performed using the application credentials configured for this service.
   *
   * @param accessToken Facebook access token to validate.
   * @return token validation details returned by Facebook.
   * @throws IntegrationException if the Facebook API returns an error or an invalid response.
   */
  public FbTokenResponse validateAccessToken(String accessToken) {
    return getFbResponse(
        uriBuilder ->
            uriBuilder
                .path("/debug_token")
                .queryParam("access_token", appToken())
                .queryParam("input_token", accessToken)
                .build(),
        FbTokenResponse.class);
  }

  /**
   * Executes a Facebook Graph API GET request and maps the response body to the specified type.
   *
   * @param uriFunction function used to build the request URI.
   * @param responseType target response type.
   * @param <T> response type.
   * @return the deserialized response.
   * @throws IntegrationException if the response is empty or Facebook returns an error.
   */
  private <T> T getFbResponse(Function<UriBuilder, URI> uriFunction, Class<T> responseType) {

    T response =
        applyErrorHandling(
                restClient.get().uri(uriFunction).accept(MediaType.APPLICATION_JSON).retrieve())
            .body(responseType);

    return requireResponse(response);
  }

  /**
   * Configures common error handling for Facebook API responses.
   *
   * <p>Client-side and server-side errors are translated into {@link IntegrationException}
   * instances.
   *
   * @param responseSpec response specification to decorate.
   * @return the configured response specification.
   */
  private RestClient.ResponseSpec applyErrorHandling(RestClient.ResponseSpec responseSpec) {

    return responseSpec
        .onStatus(
            HttpStatusCode::is4xxClientError,
            (request, response) -> {
              log.error("Facebook API returned client error: {}", response.getStatusCode().value());

              if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new IntegrationException(
                    FACEBOOK, HttpStatus.NOT_FOUND, "Facebook resource not found.");
              }

              throw new IntegrationException(
                  FACEBOOK, response.getStatusCode().value(), "Facebook API client error.");
            })
        .onStatus(
            HttpStatusCode::is5xxServerError,
            (request, response) -> {
              log.error("Facebook API returned server error: {}", response.getStatusCode().value());

              throw new IntegrationException(
                  FACEBOOK, response.getStatusCode().value(), "Facebook servers are down.");
            });
  }

  /**
   * Ensures that a response body was returned by Facebook.
   *
   * @param response response to validate.
   * @param <T> response type.
   * @return the validated response.
   * @throws IntegrationException if the response is {@code null}.
   */
  private <T> T requireResponse(T response) {
    if (response == null) {
      throw new IntegrationException(
          FACEBOOK, HttpStatus.INTERNAL_SERVER_ERROR, "Facebook returned an empty response.");
    }

    return response;
  }

  /**
   * Builds the Facebook application access token used by the Debug Token API.
   *
   * @return the application access token in the format {@code appId|appSecret}.
   */
  private String appToken() {
    return fcCredentials.appId() + "|" + fcCredentials.appSecret();
  }
}
