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
  @Test
  @DisplayName("Create figurine using JSON payload")
  void createFigurine_usingJsonPayload_shouldReturnCreatedFigurine() throws Exception {

    // --- Prepopulate required catalogs
    long distributorId = createDistributor(new DistributorReq(BANDAI, JP, null));
    long distributionId = createCatalog(distributions, "Stores");
    long lineupId = createCatalog(lineups, "Myth Cloth EX");
    long seriesId = createCatalog(series, "Saint Seiya");
    long groupId = createCatalog(groups, "Bronze Saint V1");
    long anniversaryId =
        createAnniversary(new AnniversaryReq("Masami Kurumada 40th Anniversary", 40));

    // --- Load and hydrate JSON payload
    String payload =
        loadJson(
            "payloads/figurines/create-figurine.json",
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
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().name()).isEqualTo("Pegasus Seiya");
    assertThat(response.getBody().id()).isNotNull();
  }

  /** Loads a JSON file and replaces {{placeholders}} with values. */
  private String loadJson(String path, Map<String, Object> values) throws Exception {
    String json =
        StreamUtils.copyToString(
            new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8);

    for (Map.Entry<String, Object> entry : values.entrySet()) {
      json = json.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
    }

    return json;
  }
}
