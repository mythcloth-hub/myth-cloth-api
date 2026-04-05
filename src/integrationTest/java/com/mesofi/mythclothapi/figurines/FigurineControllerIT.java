package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.dto.PaginatedResponse;
import com.mesofi.mythclothapi.it.CatalogSelector;
import com.mesofi.mythclothapi.it.FigurineScenario;
import com.mesofi.mythclothapi.it.FigurineScenarioContext;
import com.mesofi.mythclothapi.it.FigurineScenarioExtension;
import com.mesofi.mythclothapi.it.ScenarioArtifact;
import com.mesofi.mythclothapi.it.ScenarioRequest;
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
@Disabled
@ActiveProfiles("integration")
@ExtendWith(FigurineScenarioExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class FigurineControllerIT {

  private static final Logger log = LoggerFactory.getLogger(FigurineControllerIT.class);

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

  /** Verifies creation of a released figurine intended to be read later. */
  @Test
  @FigurineScenario(
      name = "A released figurine is initially created and later will be queried",
      payloads = {
        @ScenarioRequest(
            resource = "released_revival_figurine_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Store",
                    lineUp = "Myth Cloth EX",
                    series = "Saint Seiya",
                    group = "Gold Saint")),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_revival_figurine_create.json"),
        @ScenarioRequest(
            id = "queried-figurine-id-resp",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource =
                "released_revival_figurine_create.json") // it is OK to expect the same response.
      })
  void retrieveReleasedFigurine_queryExistingFigurine(FigurineScenarioContext ctx) {
    long figurineIdCreated = assertFigurineCreated(ctx);
    assertFigurineQueriedById(ctx, figurineIdCreated);
  }

  /** Verifies creation of multiple figurines intended to be read later via pagination. */
  @Test
  @FigurineScenario(
      name =
          "Multiple figurines are initially created and can later be retrieved through pagination.",
      payloads = {
        @ScenarioRequest(
            id = "p1-req",
            resource = "released_figurine_pagination1_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Nations",
                    lineUp = "Myth Cloth",
                    series = "Saint Seiya",
                    group = "Bronze Saint V3")),
        @ScenarioRequest(
            id = "p1-resp",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_figurine_pagination1_create.json"),
        @ScenarioRequest(
            id = "p2-req",
            resource = "released_figurine_pagination2_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Nations",
                    lineUp = "Myth Cloth",
                    series = "Saint Seiya",
                    group = "Bronze Saint V3")),
        @ScenarioRequest(
            id = "p2-resp",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_figurine_pagination2_create.json"),
        @ScenarioRequest(
            id = "p3-req",
            resource = "released_figurine_pagination3_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Nations",
                    lineUp = "Myth Cloth",
                    series = "Saint Seiya",
                    group = "Bronze Saint V3")),
        @ScenarioRequest(
            id = "p3-resp",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_figurine_pagination3_create.json"),
        @ScenarioRequest(
            id = "p4-req",
            resource = "released_figurine_pagination4_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Nations",
                    lineUp = "Myth Cloth",
                    series = "Saint Seiya",
                    group = "Bronze Saint V3")),
        @ScenarioRequest(
            id = "p4-resp",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_figurine_pagination4_create.json"),
        @ScenarioRequest(
            id = "p5-req",
            resource = "released_figurine_pagination5_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Nations",
                    lineUp = "Myth Cloth",
                    series = "Saint Seiya",
                    group = "Bronze Saint V3")),
        @ScenarioRequest(
            id = "p5-resp",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_figurine_pagination5_create.json"),
        @ScenarioRequest(
            id = "p-resp",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_figurine_pagination_create.json"),
      })
  void retrieveAllFigurinesByPagination_queryExistingFigurines(FigurineScenarioContext ctx) {
    for (int i = 1; i <= 5; i++) {
      assertFigurineCreated(ctx, "p" + i + "-req", "p" + i + "-resp");
    }
    assertFigurineQueriedByPagination(ctx);
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

  /** Verifies creation of an unreleased figurine intended to be updated later. */
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

  /** Verifies creation of a released figurine intended to be updated later. */
  @Test
  @FigurineScenario(
      name =
          "A released figurine is initially created with the available information and is later updated to "
              + "include the release date and an additional distributor.",
      payloads = {
        @ScenarioRequest(
            resource = "released_figurine_available_data_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Web Shop",
                    lineUp = "Myth Cloth EX",
                    series = "Saint Seiya",
                    group = "Gold Inheritor")),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_figurine_available_data_create.json"),
        @ScenarioRequest(
            id = "updated-figurine-id-req",
            resource = "released_figurine_available_data_update.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Web Shop",
                    lineUp = "Myth Cloth EX",
                    series = "Saint Seiya",
                    group = "Gold Inheritor")),
        @ScenarioRequest(
            id = "updated-figurine-id-resp",
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_figurine_available_data_update.json"),
      })
  void updateReleasedFigurine_updatesReleaseDateAndDistributor(FigurineScenarioContext ctx) {
    long figurineIdCreated = assertFigurineCreated(ctx);
    assertFigurineUpdated(ctx, figurineIdCreated);
  }

  /** Verifies creation of a released figurine intended to be deleted later. */
  @Test
  @FigurineScenario(
      name =
          "A released figurine is initially created with the available information and is later deleted",
      payloads = {
        @ScenarioRequest(
            resource = "released_classic_figurine_create.json",
            catalog =
                @CatalogSelector(
                    distribution = "Tamashii Web Shop",
                    lineUp = "Myth Cloth",
                    series = "Saint Seiya",
                    group = "Silver Saint")),
        @ScenarioRequest(
            type = ScenarioRequest.Type.EXPECTED_RESPONSE,
            resource = "released_classic_figurine_create.json")
      })
  void deleteExistingFigurine_returnsNoContent(FigurineScenarioContext ctx) {
    long figurineIdCreated = assertFigurineCreated(ctx);
    assertFigurineDeleted(figurineIdCreated, ctx.restClient());
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

    // Build HTTP request using raw JSON from a scenario
    JsonNode reqJsonNode =
        StringUtils.hasText(reqFigurineId)
            ? findJsonNodeById(ctx, reqFigurineId)
            : getPayloadAsJsonNode(ctx.payloads(), ScenarioRequest.Type.REQUEST);
    JsonNode respJsonNode =
        StringUtils.hasText(respFigurineId)
            ? findJsonNodeById(ctx, respFigurineId)
            : getPayloadAsJsonNode(ctx.payloads(), ScenarioRequest.Type.EXPECTED_RESPONSE);

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
    RestClient rest = ctx.restClient();

    JsonNode jsonNodeReq = findJsonNodeById(ctx, "updated-figurine-id-req");
    JsonNode jsonNodeResp = findJsonNodeById(ctx, "updated-figurine-id-resp");

    log.info("Updating figurine with new payload: {}", jsonNodeReq);

    // Execute PUT /figurines
    ResponseEntity<FigurineResp> response =
        rest.put()
            .uri(FIGURINES + "/{id}", figurineIdCreated)
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonNodeReq.toString())
            .retrieve()
            .toEntity(FigurineResp.class);

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
   * Executes a {@code GET /figurines/{id}} request and asserts that the figurine can be
   * successfully retrieved.
   *
   * <p>This method performs the following validations:
   *
   * <ul>
   *   <li>Retrieves the expected response payload using a scenario artifact ID
   *   <li>HTTP status is {@code 200 OK}
   *   <li>Response headers include {@code Content-Type: application/json}
   *   <li>Response body matches the expected JSON payload defined in the scenario
   * </ul>
   *
   * <p>The response body is normalized before comparison to avoid failures due to JSON field
   * ordering or formatting differences.
   *
   * @param ctx scenario context containing the expected response payload
   * @param figurineIdCreated ID of the figurine to be queried
   * @throws IllegalStateException if the expected scenario payload is missing
   */
  private void assertFigurineQueriedById(FigurineScenarioContext ctx, long figurineIdCreated) {
    RestClient rest = ctx.restClient();

    JsonNode jsonNodeResp = findJsonNodeById(ctx, "queried-figurine-id-resp");

    // Execute GET /figurines
    ResponseEntity<FigurineResp> response =
        rest.get()
            .uri(FIGURINES + "/{id}", figurineIdCreated)
            .retrieve()
            .toEntity(FigurineResp.class);

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

  private void assertFigurineQueriedByPagination(FigurineScenarioContext ctx) {
    RestClient rest = ctx.restClient();

    JsonNode jsonNodeResp = findJsonNodeById(ctx, "p-resp");

    // Execute GET /figurines
    ResponseEntity<PaginatedResponse> response =
        rest.get().uri(FIGURINES).retrieve().toEntity(PaginatedResponse.class);

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
   * Executes a {@code DELETE /figurines/{id}} request and asserts that the figurine is successfully
   * deleted.
   *
   * <p>This method performs the following validations:
   *
   * <ul>
   *   <li>Invokes the delete endpoint for the given figurine ID
   *   <li>Ensures the request completes without errors
   * </ul>
   *
   * <p>Any failure during deletion will result in the test failing due to an unexpected exception
   * or HTTP error response.
   *
   * @param figurineIdCreated ID of the figurine to be deleted
   */
  private void assertFigurineDeleted(long figurineIdCreated, RestClient rest) {
    log.info("Deleting figurine with id: {}", figurineIdCreated);

    // Execute DELETE /figurines/{id}
    ResponseEntity<Void> response =
        rest.delete().uri(FIGURINES + "/{id}", figurineIdCreated).retrieve().toBodilessEntity();

    // Basic HTTP contract assertions
    assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
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
