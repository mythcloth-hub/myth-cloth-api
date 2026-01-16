package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.it.*;
import com.mesofi.mythclothapi.utils.JsonTestUtils;

/**
 * Integration tests for {@code FigurineController} covering figurine creation scenarios.
 *
 * <p>These tests validate the {@code POST /figurines} endpoint using scenario-driven inputs and
 * expected outputs defined as JSON artifacts.
 *
 * <p>Each test is executed with {@link FigurineScenarioExtension}, which injects a {@link
 * FigurineScenarioContext} containing request and expected response payloads, as well as catalog
 * metadata resolved through {@link CatalogSelector}.
 *
 * <p>The tests assert:
 *
 * <ul>
 *   <li>HTTP contract (status, headers, location)
 *   <li>Response body structure and content
 *   <li>Correct creation of figurines across different catalog configurations
 * </ul>
 */
@ExtendWith(FigurineScenarioExtension.class)
public class FigurineControllerIT extends AbstractIntegrationTest {

  /**
   * Verifies that a prototype figurine can be created successfully.
   *
   * <p>Uses a scenario-defined request payload and validates that the API responds with {@code 201
   * Created} and a response body matching the expected scenario output.
   */
  @Test
  @FigurineScenario(
      name = "Create prototype figurine",
      payloads = {
        @ScenarioRequest(
            resource = "it_create_prototype_figurine.json",
            catalog =
                @CatalogSelector(
                    lineUp = "Myth Cloth EX",
                    series = "Saintia Sho",
                    group = "Gold Saint")),
        @ScenarioRequest(
            resource = "it_create_prototype_figurine.json",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE)
      })
  void createPrototypeFigurine_returnsCreated(FigurineScenarioContext context) {
    assertFigurineCreated(context);
  }

  /** Verifies creation of a released figurine with standard catalog distribution. */
  @Test
  @FigurineScenario(
      name = "Create released figurine",
      payloads = {
        @ScenarioRequest(
            type = ScenarioRequest.Type.REQUEST,
            resource = "it_create_released_figurine.json",
            catalog =
                @CatalogSelector(
                    distribution = "Stores",
                    lineUp = "Myth Cloth",
                    series = "Saint Seiya",
                    group = "Bronze Saint V2")),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "it_create_released_figurine.json")
      })
  void createReleasedFigurine_returnsCreated(FigurineScenarioContext context) {
    assertFigurineCreated(context);
  }

  /** Verifies creation of a released anniversary figurine with anniversary metadata applied. */
  @Test
  @FigurineScenario(
      name = "Create released with anniversary figurine",
      payloads = {
        @ScenarioRequest(
            type = ScenarioRequest.Type.REQUEST,
            resource = "it_create_released_anniversary_figurine.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Web Shop",
                    lineUp = "Myth Cloth EX",
                    series = "Saint Seiya",
                    group = "Gold Saint",
                    anniversary = 20)),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "it_create_released_anniversary_figurine.json")
      })
  void createReleasedAnniversaryFigurine_returnsCreated(FigurineScenarioContext context) {
    assertFigurineCreated(context);
  }

  /** Verifies creation of a Hong Kong (HK) limited release figurine. */
  @Test
  @FigurineScenario(
      name = "Create a release HK figurine",
      payloads = {
        @ScenarioRequest(
            type = ScenarioRequest.Type.REQUEST,
            resource = "it_create_released_hk_figurine.json",
            catalog =
                @CatalogSelector(
                    distribution = "Other Limited Edition",
                    lineUp = "Myth Cloth EX",
                    series = "Saint Seiya",
                    group = "Gold Saint")),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "it_create_released_hk_figurine.json")
      })
  void createReleasedHKFigurine_returnsCreated(FigurineScenarioContext context) {
    assertFigurineCreated(context);
  }

  /** Verifies creation of a released figurine intended to be updated later. */
  @Test
  @FigurineScenario(
      name = "Create a released figurine to be updated",
      payloads = {
        @ScenarioRequest(
            type = ScenarioRequest.Type.REQUEST,
            resource = "it_create_released_updated_figurine.json",
            catalog =
                @CatalogSelector(
                    distribution = "Stores",
                    lineUp = "DD Panoramation",
                    series = "Saint Seiya",
                    group = "Bronze Saint V1")),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "it_create_released_updated_figurine.json")
      })
  void createReleasedToBeUpdatedFigurine_returnsUpdated(FigurineScenarioContext context) {
    long figurineIdCreated = assertFigurineCreated(context);
  }

  /**
   * Executes a {@code POST /figurines} request using scenario-provided payloads and asserts that
   * the figurine is successfully created.
   *
   * <p>This method performs the following validations:
   *
   * <ul>
   *   <li>HTTP status is {@code 201 Created}
   *   <li>Response headers include {@code Content-Type: application/json}
   *   <li>A {@code Location} header is present
   *   <li>Response body matches the expected JSON payload defined in the scenario
   * </ul>
   *
   * <p>The response body is normalized before comparison to avoid failures due to JSON field
   * ordering or formatting differences.
   *
   * @param context scenario context containing request and expected response payloads
   * @return the ID of the created figurine, extracted from the {@code Location} header
   * @throws IllegalStateException if required scenario payloads are missing
   */
  private long assertFigurineCreated(FigurineScenarioContext context) {
    // Prepare request headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Build HTTP request using raw JSON from a scenario
    String requestPayload = getPayloadAsText(context.payloads(), ScenarioRequest.Type.REQUEST);
    JsonNode responsePayload =
        getPayloadAsJsonNode(context.payloads(), ScenarioRequest.Type.EXPECTED_RESPONSE);

    HttpEntity<String> request = new HttpEntity<>(requestPayload, headers);

    // Execute POST /figurines
    ResponseEntity<FigurineResp> response =
        rest.postForEntity(FIGURINES, request, FigurineResp.class);

    // Basic HTTP contract assertions
    HttpHeaders httpHeaders = response.getHeaders();

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(httpHeaders.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(httpHeaders.getLocation()).isNotNull();

    // Convert response DTO to JSON for structural comparison
    JsonNode actual = FigurineScenarioExtension.mapper.valueToTree(response.getBody());

    // Normalize JSON to avoid ordering or formatting differences
    JsonTestUtils.normalize(responsePayload);
    JsonTestUtils.normalize(actual);

    // Assert response payload matches expected scenario output
    assertThat(actual.toString()).isEqualTo(responsePayload.toString());

    URI uri = httpHeaders.getLocation();
    Assertions.assertNotNull(uri);
    String path = uri.getPath();
    return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
  }

  /**
   * Retrieves the scenario payload as a raw JSON string for the given request type.
   *
   * @param payloads scenario artifacts
   * @param type payload type to retrieve
   * @return JSON payload as a string
   */
  private String getPayloadAsText(List<ScenarioArtifact> payloads, ScenarioRequest.Type type) {
    return getPayload(payloads, type).toString();
  }

  /**
   * Retrieves the scenario payload as a {@link JsonNode} for the given payload type.
   *
   * @param payloads scenario artifacts
   * @param type payload type to retrieve
   * @return JSON payload as a {@link JsonNode}
   */
  private JsonNode getPayloadAsJsonNode(
      List<ScenarioArtifact> payloads, ScenarioRequest.Type type) {
    return getPayload(payloads, type);
  }

  /**
   * Locates the first scenario artifact matching the given type and returns its JSON content.
   *
   * @param payloads scenario artifacts
   * @param type payload type to retrieve
   * @return JSON content of the matching artifact
   * @throws IllegalStateException if no artifact matches the given type
   */
  private JsonNode getPayload(List<ScenarioArtifact> payloads, ScenarioRequest.Type type) {
    return payloads.stream()
        .filter(p -> p.type() == type)
        .map(ScenarioArtifact::json)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No payload found for type: " + type));
  }
}
