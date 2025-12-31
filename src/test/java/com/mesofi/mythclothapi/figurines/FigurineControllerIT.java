package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

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
   * Verifies that creating a released anniversary figurine returns HTTP 201 and includes
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
   * Executes the HTTP POST request to create a figurine and performs all common assertions shared
   * by creation scenarios.
   *
   * <p>This method ensures:
   *
   * <ul>
   *   <li>HTTP status is 201 (Created)
   *   <li>Response body is present
   *   <li>Response content type is JSON
   *   <li>Response payload matches the expected JSON (after normalization)
   * </ul>
   *
   * @param context scenario context provided by {@link CatalogScenarioExtension}
   */
  private void assertFigurineCreated(ScenarioContext context) {
    // Prepare request headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Build HTTP request using raw JSON from scenario
    HttpEntity<String> request = new HttpEntity<>(context.request().raw(), headers);

    // Execute POST /figurines
    ResponseEntity<FigurineResp> response =
        rest.postForEntity(FIGURINES, request, FigurineResp.class);

    // Basic HTTP contract assertions
    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

    // Convert response DTO to JSON for structural comparison
    JsonNode expected = context.expected().json();
    JsonNode actual = CatalogScenarioExtension.mapper.valueToTree(response.getBody());

    // Normalize JSON to avoid ordering or formatting differences
    JsonTestUtils.normalize(expected);
    JsonTestUtils.normalize(actual);

    // Assert response payload matches expected scenario output
    assertThat(actual.toString()).isEqualTo(expected.toString());
  }
}
