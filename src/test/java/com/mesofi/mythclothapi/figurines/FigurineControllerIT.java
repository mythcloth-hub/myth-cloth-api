package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.it.AbstractIntegrationTest;
import com.mesofi.mythclothapi.it.CatalogScenarioExtension;
import com.mesofi.mythclothapi.it.CatalogSelector;
import com.mesofi.mythclothapi.it.FigurineScenario;
import com.mesofi.mythclothapi.it.ScenarioContext;
import com.mesofi.mythclothapi.it.ScenarioData;

@ExtendWith(CatalogScenarioExtension.class)
public class FigurineControllerIT extends AbstractIntegrationTest {

  @Test
  @FigurineScenario(
      catalog =
          @CatalogSelector(lineUp = "Myth Cloth EX", series = "Saintia Sho", group = "Gold Saint"),
      data =
          @ScenarioData(
              name = "Prototype Figurine",
              request = "it_create_figurine_prototype.json",
              expectedResponse = "it_create_figurine_prototype.json"))
  void createFigurine_givenValidScenario_shouldCreateFigurine(ScenarioContext context) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(context.request().raw(), headers);

    // --- Execute request
    ResponseEntity<FigurineResp> response =
        rest.postForEntity(FIGURINES, request, FigurineResp.class);

    // --- Assertions
    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    JsonNode expectedJson = context.expected().json();
    JsonNode actualJson = CatalogScenarioExtension.mapper.valueToTree(response.getBody());

    removeTimestamps(expectedJson);
    removeTimestamps(actualJson);

    assertThat(actualJson.toString()).isEqualTo(expectedJson.toString());
  }

  private void removeTimestamps(JsonNode node) {
    if (node.isObject()) {
      ((ObjectNode) node).remove(List.of("createdAt", "updatedAt"));
    }
  }
}
