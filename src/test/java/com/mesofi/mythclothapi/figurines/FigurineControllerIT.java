package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;

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

  private static final Logger log = LoggerFactory.getLogger(FigurineControllerIT.class);

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
            resource = "prototype_figurine_create.json",
            catalog =
                @CatalogSelector(
                    lineUp = "Myth Cloth EX",
                    series = "Saintia Sho",
                    group = "Gold Saint")),
        @ScenarioRequest(
            resource = "prototype_figurine_create.json",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE)
      })
  void createPrototypeFigurine_returnsCreated(FigurineScenarioContext ctx) {
    assertFigurineCreated(ctx);
  }

  /** Verifies creation of a released figurine with standard catalog distribution. */
  @Test
  @FigurineScenario(
      name = "Create released figurine",
      payloads = {
        @ScenarioRequest(
            type = ScenarioRequest.Type.REQUEST,
            resource = "released_figurine_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Stores",
                    lineUp = "Myth Cloth",
                    series = "Saint Seiya",
                    group = "Bronze Saint V2")),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_figurine_create.json")
      })
  void createReleasedFigurine_returnsCreated(FigurineScenarioContext ctx) {
    assertFigurineCreated(ctx);
  }

  /** Verifies creation of a released anniversary figurine with anniversary metadata applied. */
  @Test
  @FigurineScenario(
      name = "Create released with anniversary figurine",
      payloads = {
        @ScenarioRequest(
            type = ScenarioRequest.Type.REQUEST,
            resource = "released_anniversary_figurine_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Web Shop",
                    lineUp = "Myth Cloth EX",
                    series = "Saint Seiya",
                    group = "Gold Saint",
                    anniversary = 20)),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_anniversary_figurine_create.json")
      })
  void createReleasedAnniversaryFigurine_returnsCreated(FigurineScenarioContext ctx) {
    assertFigurineCreated(ctx);
  }

  /** Verifies creation of a Hong Kong (HK) limited release figurine. */
  @Test
  @FigurineScenario(
      name = "Create a release HK figurine",
      payloads = {
        @ScenarioRequest(
            type = ScenarioRequest.Type.REQUEST,
            resource = "released_hk_figurine_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Other Limited Edition",
                    lineUp = "Myth Cloth EX",
                    series = "Saint Seiya",
                    group = "Gold Saint")),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_hk_figurine_create.json")
      })
  void createReleasedHKFigurine_returnsCreated(FigurineScenarioContext ctx) {
    assertFigurineCreated(ctx);
  }

  /** Verifies creation of a released figurine intended to be updated later. */
  @Test
  @FigurineScenario(
      name =
          "A released figurine is initially created with an invalid Tamashii URL and is later updated to store the correct value.",
      payloads = {
        @ScenarioRequest(
            resource = "released_invalid_tamashii_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Stores",
                    lineUp = "Myth Cloth",
                    series = "Saint Seiya",
                    group = "Bronze Saint V2")),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_invalid_tamashii_create.json"),
        @ScenarioRequest(
            id = "updated-figurine-id-req",
            resource = "released_invalid_tamashii_update.json",
            catalog =
                @CatalogSelector(
                    distribution = "Stores",
                    lineUp = "Myth Cloth",
                    series = "Saint Seiya",
                    group = "Bronze Saint V2")),
        @ScenarioRequest(
            id = "updated-figurine-id-resp",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_invalid_tamashii_update.json"),
      })
  void updateReleasedFigurine_updatesOnlyOneField(FigurineScenarioContext ctx) {
    long figurineIdCreated = assertFigurineCreated(ctx);
    assertFigurineUpdated(ctx, figurineIdCreated);
  }

  /** Verifies creation of a prototype figurine intended to be updated later. */
  @Test
  @FigurineScenario(
      name =
          "A prototype figurine is initially created with incorrect field values and is later updated with the correct information.",
      payloads = {
        @ScenarioRequest(
            resource = "prototype_invalid_info_create.json",
            catalog =
                @CatalogSelector(
                    lineUp = "Myth Cloth EX",
                    series = "Saint Seiya",
                    group = "Gold Saint")),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "prototype_invalid_info_create.json"),
        @ScenarioRequest(
            id = "updated-figurine-id-req",
            resource = "prototype_invalid_info_update.json",
            catalog =
                @CatalogSelector(
                    lineUp = "Myth Cloth EX",
                    series = "Saint Seiya",
                    group = "Gold Saint")),
        @ScenarioRequest(
            id = "updated-figurine-id-resp",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "prototype_invalid_info_update.json"),
      })
  void updateReleasedFigurine_updatesMultipleFields(FigurineScenarioContext ctx) {
    long figurineIdCreated = assertFigurineCreated(ctx);
    assertFigurineUpdated(ctx, figurineIdCreated);
  }

  /** Verifies creation of a unreleased figurine intended to be updated later. */
  @Test
  @FigurineScenario(
      name =
          "An unreleased figurine is initially created with incorrect references and later updated with the correct information.",
      payloads = {
        @ScenarioRequest(
            resource = "unreleased_invalid_references_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Other Limited Edition",
                    lineUp = "Figuarts",
                    series = "Saint Seiya",
                    group = "Steel")),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "unreleased_invalid_references_create.json"),
        @ScenarioRequest(
            id = "updated-figurine-id-req",
            resource = "unreleased_invalid_references_update.json",
            catalog =
                @CatalogSelector(lineUp = "Myth Cloth EX", series = "Saint Seiya", group = "God")),
        @ScenarioRequest(
            id = "updated-figurine-id-resp",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "unreleased_invalid_references_update.json"),
      })
  void updateUnReleasedFigurine_updatesReferenceFields(FigurineScenarioContext ctx) {
    long figurineIdCreated = assertFigurineCreated(ctx);
    assertFigurineUpdated(ctx, figurineIdCreated);
  }

  /** Verifies creation of a released figurine intended to be updated later. */
  @Test
  @FigurineScenario(
      name =
          "An released figurine is initially created with invalid images and later updated with the correct url's.",
      payloads = {
        @ScenarioRequest(
            resource = "released_figurine_invalid_images_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Web Shop",
                    lineUp = "Myth Cloth EX",
                    series = "Saint Seiya",
                    group = "Judge")),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_figurine_invalid_images_create.json"),
        @ScenarioRequest(
            id = "updated-figurine-id-req",
            resource = "released_figurine_invalid_images_update.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Web Shop",
                    lineUp = "Myth Cloth EX",
                    series = "Saint Seiya",
                    group = "Judge")),
        @ScenarioRequest(
            id = "updated-figurine-id-resp",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_figurine_invalid_images_update.json"),
      })
  void updateReleasedFigurine_updatesImagesUrls(FigurineScenarioContext ctx) {
    long figurineIdCreated = assertFigurineCreated(ctx);
    assertFigurineUpdated(ctx, figurineIdCreated);
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
   * @param ctx scenario context containing request and expected response payloads
   * @return the ID of the created figurine, extracted from the {@code Location} header
   * @throws IllegalStateException if required scenario payloads are missing
   */
  private long assertFigurineCreated(FigurineScenarioContext ctx) {
    // Prepare request headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Build HTTP request using raw JSON from a scenario
    String req = getPayloadAsText(ctx.payloads(), ScenarioRequest.Type.REQUEST);
    JsonNode resp = getPayloadAsJsonNode(ctx.payloads(), ScenarioRequest.Type.EXPECTED_RESPONSE);

    HttpEntity<String> request = new HttpEntity<>(req, headers);

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
    JsonTestUtils.normalize(resp);
    JsonTestUtils.normalize(actual);

    // Assert response payload matches expected scenario output
    assertThat(actual.toString()).isEqualTo(resp.toString());

    URI uri = httpHeaders.getLocation();
    Assertions.assertNotNull(uri);
    String path = uri.getPath();
    return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
  }

  /**
   * Executes a {@code PUT /figurines/{id}} request using scenario-provided update payloads and
   * asserts that the figurine is successfully updated.
   *
   * <p>This method performs the following validations:
   *
   * <ul>
   *   <li>Retrieves request and expected response payloads using scenario artifact IDs
   *   <li>HTTP status is {@code 200 OK}
   *   <li>Response headers include {@code Content-Type: application/json}
   *   <li>Response body matches the expected JSON payload defined in the scenario
   * </ul>
   *
   * <p>The response body is normalized before comparison to avoid failures due to JSON field
   * ordering or formatting differences.
   *
   * @param ctx scenario context containing update request and expected response payloads
   * @param figurineIdCreated ID of the figurine to be updated
   * @throws IllegalStateException if required scenario payloads are missing
   */
  private void assertFigurineUpdated(FigurineScenarioContext ctx, long figurineIdCreated) {
    JsonNode jsonNodeReq = findJsonNodeById(ctx, "updated-figurine-id-req");
    JsonNode jsonNodeResp = findJsonNodeById(ctx, "updated-figurine-id-resp");

    log.info("Updating figurine with new payload: {}", jsonNodeReq);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(jsonNodeReq.toString(), headers);

    // Execute PUT /figurines
    ResponseEntity<FigurineResp> response =
        rest.exchange(
            FIGURINES + "/{id}", HttpMethod.PUT, request, FigurineResp.class, figurineIdCreated);

    // Basic HTTP contract assertions
    HttpHeaders httpHeaders = response.getHeaders();

    // Basic HTTP contract assertions
    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(httpHeaders.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

    // Convert response DTO to JSON for structural comparison
    JsonNode actual = FigurineScenarioExtension.mapper.valueToTree(response.getBody());

    // Normalize JSON to avoid ordering or formatting differences
    JsonTestUtils.normalize(jsonNodeResp);
    JsonTestUtils.normalize(actual);

    // Assert response payload matches expected scenario output
    assertThat(actual.toString()).isEqualTo(jsonNodeResp.toString());
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

  /**
   * Locates a scenario artifact by its identifier and returns its JSON payload.
   *
   * <p>This method is primarily used for scenarios containing multiple request or response payloads
   * where a specific artifact must be selected explicitly.
   *
   * @param ctx scenario context containing all scenario artifacts
   * @param id unique identifier of the scenario artifact
   * @return JSON payload associated with the given artifact ID
   * @throws IllegalStateException if no artifact with the given ID exists in the context
   */
  private JsonNode findJsonNodeById(FigurineScenarioContext ctx, String id) {
    return ctx.payloads().stream()
        .filter(scenarioArtifact -> scenarioArtifact.id().equals(id))
        .findFirst()
        .map(ScenarioArtifact::json)
        .orElseThrow(() -> new IllegalStateException("No payload found in context for id: " + id));
  }
}
