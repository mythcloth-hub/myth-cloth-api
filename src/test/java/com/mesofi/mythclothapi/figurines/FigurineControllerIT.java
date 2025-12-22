package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.catalogs.dto.CatalogType.distributions;
import static com.mesofi.mythclothapi.catalogs.dto.CatalogType.groups;
import static com.mesofi.mythclothapi.catalogs.dto.CatalogType.lineups;
import static com.mesofi.mythclothapi.catalogs.dto.CatalogType.series;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.it.AbstractIntegrationTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FigurineControllerIT extends AbstractIntegrationTest {

  Logger log = LoggerFactory.getLogger(FigurineControllerIT.class);

  final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // optional but recommended

  private List<DistributorResp> distributorRespList;
  private List<CatalogResp> distributionRespList;
  private List<CatalogResp> lineupRespList;
  private List<CatalogResp> seriesRespList;
  private List<CatalogResp> groupRespList;
  private List<AnniversaryResp> anniversariesReqList;

  @BeforeAll
  void setUpCatalogs() {
    distributorRespList = createDistributors();
    distributionRespList = createCatalogs(distributions);
    lineupRespList = createCatalogs(lineups);
    seriesRespList = createCatalogs(series);
    groupRespList = createCatalogs(groups);
    anniversariesReqList = createAnniversaries();
  }

  @AfterAll
  void cleanUpCatalogs() {
    // jdbc.execute("DELETE FROM distributors");
  }

  @AfterEach
  void deleteFigurines() {}

  @ParameterizedTest(name = "{index} ⇒ {0}")
  @MethodSource("createFigurineScenarios")
  @DisplayName("Create figurine scenarios")
  void createFigurine_usingJsonPayload_shouldReturnCreatedFigurine(
      String description, String requestPayloadPath, String expectedResponsePath) throws Exception {

    log.info("Create figurine scenarios: {}", description);

    // --- Load and hydrate JSON payload
    String payload =
        loadJson(
            requestPayloadPath,
            Map.of(
                "supplierId",
                distributorRespList.getFirst().id(),
                "distributionId",
                distributionRespList.getFirst().id(),
                "lineUpId",
                lineupRespList.getFirst().id(),
                "seriesId",
                seriesRespList.getFirst().id(),
                "groupId",
                groupRespList.getFirst().id(),
                "anniversaryId",
                anniversariesReqList.getFirst().id()));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(payload, headers);

    // --- Execute request
    ResponseEntity<FigurineResp> response =
        rest.postForEntity(FIGURINES, request, FigurineResp.class);

    // --- Assertions
    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    String expectedJsonString = loadJson(expectedResponsePath, null);

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

  static Stream<Arguments> createFigurineScenarios() {
    return Stream.of(
        Arguments.of(
            "Standard unreleased figurine",
            "payloads/figurines/request/it_create_figurine_to_be_released.json",
            "payloads/figurines/request/it_create_figurine_to_be_released.json"),
        Arguments.of(
            "Released Figurine",
            "payloads/figurines/request/it_create_figurine_released.json",
            "payloads/figurines/response/it_create_figurine_released.json"),
        Arguments.of(
            "Prototype or unreleased",
            "payloads/figurines/request/it_create_figurine_prototype.json",
            "payloads/figurines/response/it_create_figurine_prototype.json"));
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
