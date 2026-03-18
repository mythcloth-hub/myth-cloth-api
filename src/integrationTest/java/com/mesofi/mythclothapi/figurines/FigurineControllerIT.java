package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.it.CatalogSelector;
import com.mesofi.mythclothapi.it.FigurineScenario;
import com.mesofi.mythclothapi.it.FigurineScenarioContext;
import com.mesofi.mythclothapi.it.FigurineScenarioExtension;
import com.mesofi.mythclothapi.it.ScenarioArtifact;
import com.mesofi.mythclothapi.it.ScenarioRequest;
import com.mesofi.mythclothapi.utils.JsonTestUtils;

@ExtendWith(FigurineScenarioExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class FigurineControllerIT {

  /**
   * Base endpoint path for figurine-related operations.
   *
   * <p>Exposed as a constant to avoid hard-coded URL strings across test classes.
   */
  protected static final String FIGURINES = "/figurines";

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

  /**
   * Executes a {@code POST /figurines} request using scenario-provided payloads and asserts that
   * the figurine is successfully created.
   *
   * <p>This method delegates to {@link #assertFigurineCreated(FigurineScenarioContext, String,
   * String)} without specifying request or response payload IDs, meaning the default scenario
   * payloads will be used.
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
    return assertFigurineCreated(ctx, null, null);
  }

  /**
   * Executes a {@code POST /figurines} request using scenario-provided payloads and asserts that
   * the figurine is successfully created.
   *
   * <p>This variant allows selecting specific request and/or expected response payloads from the
   * scenario by their identifiers. If an identifier is {@code null} or blank, the default payload
   * for that type will be used.
   *
   * <p>This method performs the following validations:
   *
   * <ul>
   *   <li>HTTP status is {@code 201 Created}
   *   <li>Response headers include {@code Content-Type: application/json}
   *   <li>A {@code Location} header is present
   *   <li>Response body structurally matches the expected JSON payload
   * </ul>
   *
   * <p>The response body is normalized before comparison to avoid failures due to JSON field
   * ordering or formatting differences.
   *
   * @param ctx scenario context containing request and expected response payloads
   * @param reqFigurineId optional identifier of the request payload to use from the scenario
   * @param respFigurineId optional identifier of the expected response payload to use from the
   *     scenario
   * @return the ID of the created figurine, extracted from the {@code Location} header
   * @throws IllegalStateException if required scenario payloads are missing or cannot be resolved
   */
  private long assertFigurineCreated(
      FigurineScenarioContext ctx, String reqFigurineId, String respFigurineId) {

    RestClient rest = ctx.restClient();

    // Prepare request headers
    // HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_JSON);

    // Build HTTP request using raw JSON from a scenario
    JsonNode reqJsonNode =
        StringUtils.hasText(reqFigurineId)
            ? findJsonNodeById(ctx, reqFigurineId)
            : getPayloadAsJsonNode(ctx.payloads(), ScenarioRequest.Type.REQUEST);
    JsonNode respJsonNode =
        StringUtils.hasText(respFigurineId)
            ? findJsonNodeById(ctx, respFigurineId)
            : getPayloadAsJsonNode(ctx.payloads(), ScenarioRequest.Type.EXPECTED_RESPONSE);

    // HttpEntity<String> request = new HttpEntity<>(reqJsonNode.toString(), headers);

    // Execute POST /figurines
    ResponseEntity<FigurineResp> response =
        rest.post()
            .uri(FIGURINES)
            .contentType(MediaType.APPLICATION_JSON)
            .body(reqJsonNode.toString())
            .retrieve()
            .toEntity(FigurineResp.class);

    // Basic HTTP contract assertions
    HttpHeaders httpHeaders = response.getHeaders();

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(httpHeaders.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(httpHeaders.getLocation()).isNotNull();

    // Convert response DTO to JSON for structural comparison
    JsonNode actual = FigurineScenarioExtension.mapper.valueToTree(response.getBody());

    // Normalize JSON to avoid ordering or formatting differences
    JsonTestUtils.normalize(respJsonNode);
    JsonTestUtils.normalize(actual);

    // Assert response payload matches expected scenario output
    assertThat(actual.toString()).isEqualTo(respJsonNode.toString());

    URI uri = httpHeaders.getLocation();
    Assertions.assertNotNull(uri);
    String path = uri.getPath();
    return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
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
    return getFirstPayload(payloads, type);
  }

  /**
   * Locates the first scenario artifact matching the given type and returns its JSON content.
   *
   * @param payloads scenario artifacts
   * @param type payload type to retrieve
   * @return JSON content of the matching artifact
   * @throws IllegalStateException if no artifact matches the given type
   */
  private JsonNode getFirstPayload(List<ScenarioArtifact> payloads, ScenarioRequest.Type type) {
    return getAllPayloadsByType(payloads, type).stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No payload found for type: " + type));
  }

  /**
   * Retrieves all scenario payloads that match the given type.
   *
   * <p>This method is useful for scenarios containing multiple artifacts of the same type (for
   * example, multiple expected responses when validating pagination or batch operations).
   *
   * @param payloads scenario artifacts provided by {@link FigurineScenarioContext}
   * @param type the {@link ScenarioRequest.Type} of payloads to retrieve
   * @return a list of {@link JsonNode} instances corresponding to all matching artifacts; an empty
   *     list if no artifacts of the given type exist
   */
  private List<JsonNode> getAllPayloadsByType(
      List<ScenarioArtifact> payloads, ScenarioRequest.Type type) {
    return payloads.stream().filter(p -> p.type() == type).map(ScenarioArtifact::json).toList();
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
