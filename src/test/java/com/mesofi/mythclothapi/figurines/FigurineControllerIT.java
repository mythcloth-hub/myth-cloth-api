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

@ExtendWith(CatalogScenarioExtension.class)
public class FigurineControllerIT extends AbstractIntegrationTest {

  @Test
  @FigurineScenario(
      catalog =
          @CatalogSelector(lineUp = "Myth Cloth EX", series = "Saintia Sho", group = "Gold Saint"),
      data =
          @ScenarioData(
              name = "Create prototype figurine",
              request = "it_create_prototype_figurine.json",
              expectedResponse = "it_create_prototype_figurine.json"))
  void createPrototypeFigurine_shouldReturnCreatedFigurine(ScenarioContext context) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(context.request().raw(), headers);

    // --- Execute request
    ResponseEntity<FigurineResp> response =
        rest.postForEntity(FIGURINES, request, FigurineResp.class);

    // --- Assertions
    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    JsonNode expected = context.expected().json();
    JsonNode actual = CatalogScenarioExtension.mapper.valueToTree(response.getBody());

    JsonTestUtils.normalize(expected);
    JsonTestUtils.normalize(actual);

    assertThat(actual.toString()).isEqualTo(expected.toString());
  }

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
  void createReleasedFigurine_shouldReturnCreatedFigurine(ScenarioContext context) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(context.request().raw(), headers);

    // --- Execute request
    ResponseEntity<FigurineResp> response =
        rest.postForEntity(FIGURINES, request, FigurineResp.class);

    // --- Assertions
    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    JsonNode expected = context.expected().json();
    JsonNode actual = CatalogScenarioExtension.mapper.valueToTree(response.getBody());

    JsonTestUtils.normalize(expected);
    JsonTestUtils.normalize(actual);

    assertThat(actual.toString()).isEqualTo(expected.toString());
  }
}
