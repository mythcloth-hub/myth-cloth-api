package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.it.AbstractIntegrationTest;
import com.mesofi.mythclothapi.it.CatalogScenarioExtension;
import com.mesofi.mythclothapi.it.CatalogSelector;
import com.mesofi.mythclothapi.it.FigurineScenario;
import com.mesofi.mythclothapi.it.ScenarioContext;
import com.mesofi.mythclothapi.it.ScenarioData;
import com.mesofi.mythclothapi.utils.JsonTestUtils;

/**
 * Integration tests for {@code FigurineController}.
 *
 * <p>This test class verifies the creation of figurines through the HTTP API. Each test method
 * declares a {@link FigurineScenario} describing:
 *
 * <ul>
 *   <li>The catalog context (line-up, series, distribution, etc.)
 *   <li>The request payload JSON
 *   <li>The expected response JSON
 * </ul>
 *
 * <p>The {@link CatalogScenarioExtension} is responsible for:
 *
 * <ul>
 *   <li>Preparing catalog data before the test
 *   <li>Injecting a fully populated {@link ScenarioContext} into the test method
 * </ul>
 *
 * <p>All test methods delegate execution and assertions to a shared helper to avoid duplication and
 * keep scenarios declarative.
 */
@ExtendWith(CatalogScenarioExtension.class)
public class FigurineControllerIT extends AbstractIntegrationTest {

  /**
   * Verifies that creating a prototype figurine returns HTTP 201 (Created) and a response body
   * matching the expected JSON.
   */
  @Test
  @FigurineScenario(
      catalog =
          @CatalogSelector(lineUp = "Myth Cloth EX", series = "Saintia Sho", group = "Gold Saint"),
      data =
          @ScenarioData(
              name = "Create prototype figurine",
              request = "it_create_prototype_figurine.json",
              expectedResponse = "it_create_prototype_figurine.json"))
  void createPrototypeFigurine_returnsCreated(ScenarioContext context) {
    assertFigurineCreated(context);
  }

  /**
   * Verifies that creating a released figurine with store distribution returns HTTP 201 and the
   * expected response payload.
   */
  @Test
  @FigurineScenario(
      catalog =
          @CatalogSelector(
              distribution = "Stores",
              lineUp = "Myth Cloth",
              series = "Saint Seiya",
              group = "Bronze Saint V2"),
      data =
          @ScenarioData(
              name = "Create released figurine",
              request = "it_create_released_figurine.json",
              expectedResponse = "it_create_released_figurine.json"))
  void createReleasedFigurine_returnsCreated(ScenarioContext context) {
    assertFigurineCreated(context);
  }

  /**
   * Verifies that creating a future release figurine returns HTTP 201 and the expected response
   * payload.
   */
  @Test
  @FigurineScenario(
      catalog =
          @CatalogSelector(
              lineUp = "Myth Cloth EX",
              series = "Saint Seiya",
              group = "Bronze Saint V4"),
      data =
          @ScenarioData(
              name = "Create a future release figurine",
              request = "it_create_future_release_figurine.json",
              expectedResponse = "it_create_future_release_figurine.json"))
  void createFutureReleaseFigurine_returnsCreated(ScenarioContext context) {
    assertFigurineCreated(context);
  }

  /**
   * Verifies that creating a future release figurine returns HTTP 201 and includes
   * anniversary-related data in the response.
   */
  @Test
  @FigurineScenario(
      catalog =
          @CatalogSelector(
              distribution = "Tamashii Web Shop",
              lineUp = "Myth Cloth EX",
              series = "Saint Seiya",
              group = "Gold Saint",
              anniversary = 20),
      data =
          @ScenarioData(
              name = "Create released with anniversary figurine",
              request = "it_create_released_anniversary_figurine.json",
              expectedResponse = "it_create_released_anniversary_figurine.json"))
  void createReleasedAnniversaryFigurine_returnsCreated(ScenarioContext context) {
    assertFigurineCreated(context);
  }

  /**
   * Verifies that creating a released HK figurine returns HTTP 201 and the expected response
   * payload.
   */
  @Test
  @FigurineScenario(
      catalog =
          @CatalogSelector(
              distribution = "Other Limited Edition",
              lineUp = "Myth Cloth EX",
              series = "Saint Seiya",
              group = "Gold Saint"),
      data =
          @ScenarioData(
              name = "Create a release HK figurine",
              request = "it_create_released_hk_figurine.json",
              expectedResponse = "it_create_released_hk_figurine.json"))
  void createReleasedHKFigurine_returnsCreated(ScenarioContext context) {
    assertFigurineCreated(context);
  }

  /**
   * Verifies that creating a regular figurine that can be updated later. Returns HTTP 201 and the
   * expected response payload.
   */
  @Test
  @FigurineScenario(
      catalog =
          @CatalogSelector(
              distribution = "Stores",
              lineUp = "DD Panoramation",
              series = "Saint Seiya",
              group = "Bronze Saint V1"),
      data =
          @ScenarioData(
              name = "Create a released figurine to be updated",
              request = "it_create_released_updated_figurine.json",
              expectedResponse = "it_create_released_updated_figurine.json"))
  void createReleasedToBeUpdatedFigurine_returnsUpdated(ScenarioContext context) {
    long figurineIdCreated = assertFigurineCreated(context);
  }

  /**
   * Executes the {@code POST /figurines} endpoint and asserts that a figurine is successfully
   * created according to the current scenario.
   *
   * <p>This method performs the following steps:
   *
   * <ul>
   *   <li>Builds an HTTP request using the raw JSON payload provided by the scenario context
   *   <li>Sends the request to the {@code /figurines} endpoint
   *   <li>Validates the basic HTTP contract:
   *       <ul>
   *         <li>HTTP status is {@code 201 CREATED}
   *         <li>Response body is not {@code null}
   *         <li>{@code Content-Type} is {@code application/json}
   *         <li>{@code Location} header is present
   *       </ul>
   *   <li>Converts the response DTO into JSON
   *   <li>Normalizes both expected and actual JSON structures to avoid ordering or formatting
   *       differences
   *   <li>Asserts that the actual response payload matches the expected scenario output
   *   <li>Extracts and returns the created figurine ID from the {@code Location} header
   * </ul>
   *
   * @param context the scenario context containing the request payload and the expected response
   *     JSON
   * @return the identifier of the newly created figurine, extracted from the {@code Location}
   *     response header
   */
  private long assertFigurineCreated(ScenarioContext context) {
    // Prepare request headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Build HTTP request using raw JSON from scenario
    HttpEntity<String> request = new HttpEntity<>(context.request().raw(), headers);

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
    JsonNode expected = context.expected().json();
    JsonNode actual = CatalogScenarioExtension.mapper.valueToTree(response.getBody());

    // Normalize JSON to avoid ordering or formatting differences
    JsonTestUtils.normalize(expected);
    JsonTestUtils.normalize(actual);

    // Assert response payload matches expected scenario output
    assertThat(actual.toString()).isEqualTo(expected.toString());

    URI uri = httpHeaders.getLocation();
    Assertions.assertNotNull(uri);
    String path = uri.getPath();
    return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
  }
}
