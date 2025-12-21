package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.catalogs.dto.CatalogType.distributions;
import static com.mesofi.mythclothapi.catalogs.dto.CatalogType.groups;
import static com.mesofi.mythclothapi.catalogs.dto.CatalogType.lineups;
import static com.mesofi.mythclothapi.catalogs.dto.CatalogType.series;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.BANDAI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.it.AbstractIntegrationTest;

/**
 * Integration tests for {@code /figurines} endpoints.
 *
 * <p>These tests exercise the full HTTP stack, including:
 *
 * <ul>
 *   <li>Catalog prepopulation
 *   <li>Distributor creation
 *   <li>Figurine creation and response mapping
 * </ul>
 */
class FigurineControllerIT extends AbstractIntegrationTest {

  final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // optional but recommended

  @Test
  @DisplayName("Create figurine using JSON payload")
  void createFigurine_usingJsonPayload_shouldReturnCreatedFigurine() throws Exception {

    // --- Prepopulate required catalogs
    long distributorId = createDistributor(new DistributorReq(BANDAI, JP, "https://tamashii.jp/"));
    long distributionId = createCatalog(distributions, "Tamashii Web Shop");
    long lineupId = createCatalog(lineups, "Myth Cloth EX");
    long seriesId = createCatalog(series, "Saint Seiya");
    long groupId = createCatalog(groups, "Bronze Saint V3");
    long anniversaryId =
        createAnniversary(new AnniversaryReq("Masami Kurumada 40th Anniversary", 40));

    // --- Load and hydrate JSON payload
    String payload =
        loadJson(
            "payloads/figurines/request/create-figurine.json",
            Map.of(
                "supplierId",
                distributorId,
                "distributionId",
                distributionId,
                "lineUpId",
                lineupId,
                "seriesId",
                seriesId,
                "groupId",
                groupId,
                "anniversaryId",
                anniversaryId));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(payload, headers);

    // --- Execute request
    ResponseEntity<FigurineResp> response =
        rest.postForEntity(FIGURINES, request, FigurineResp.class);

    // --- Assertions
    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    String expectedJsonString = loadJson("payloads/figurines/response/create-figurine.json", null);

    JsonNode actualJson = OBJECT_MAPPER.valueToTree(response.getBody());
    JsonNode expectedJson = OBJECT_MAPPER.readTree(expectedJsonString);

    removeFields(actualJson);
    removeFields(expectedJson);

    assertThat(actualJson.asText()).isEqualTo(expectedJson.asText());
  }

  private void removeFields(JsonNode node) {
    if (node instanceof ObjectNode objectNode) {
      for (String field : new String[] {"id", "createdAt", "updatedAt"}) {
        objectNode.remove(field);
      }
    }
  }

  /** Loads a JSON file and replaces {{placeholders}} with values. */
  private String loadJson(String path, Map<String, Object> values) throws Exception {
    String json =
        StreamUtils.copyToString(
            new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8);

    if (values != null) {
      for (Map.Entry<String, Object> entry : values.entrySet()) {
        json = json.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
      }
      return json;
    }
    return json;
  }
}
