package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.catalogs.dto.CatalogType.distributions;
import static com.mesofi.mythclothapi.catalogs.dto.CatalogType.groups;
import static com.mesofi.mythclothapi.catalogs.dto.CatalogType.lineups;
import static com.mesofi.mythclothapi.catalogs.dto.CatalogType.series;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.BANDAI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogType;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurines.dto.DistributorReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FigurineControllerIT {

  private static final Logger log = LoggerFactory.getLogger(FigurineControllerIT.class);
  @Autowired private TestRestTemplate rest;

  private final String FIGURINES = "/figurines";

  @Test
  @DisplayName("Test flow to create and process figurines")
  void fullCrudFigurinesFlow() {
    long distributorId =
        createDistributor(
            new com.mesofi.mythclothapi.distributors.dto.DistributorReq(BANDAI, JP, null));
    long distributionId = createCatalog(distributions, new CatalogReq("Stores"));
    long lineupId = createCatalog(lineups, new CatalogReq("Myth Cloth EX"));
    long seriesId = createCatalog(series, new CatalogReq("Saint Seiya"));
    long groupId = createCatalog(groups, new CatalogReq("Bronze Saint V1"));

    FigurineReq req = createReqFigurine(distributorId, distributionId, lineupId, seriesId, groupId);

    // CREATE
    // TODO fix the following lines
    // Long figurineId = createFigurines(req);
    // log.info("Created a new figurine with id: {}", figurineId);
  }

  private FigurineReq createReqFigurine(
      long distributorId, long distributionId, long lineupId, long seriesId, long groupId) {

    return new FigurineReq(
        "Pegasus Seiya",
        List.of(
            new DistributorReq(
                distributorId,
                CurrencyCode.JPY,
                12500d,
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now(),
                true)),
        "https://tamashiiweb.com/item/14583",
        distributionId,
        lineupId,
        seriesId,
        groupId,
        null,
        false,
        true,
        true,
        true,
        false,
        false,
        true,
        false,
        false,
        true,
        "Some comment",
        List.of("https://imagizer.imageshack.com/img924/4413/Pl86x4.jpg"),
        List.of("https://imagizer.imageshack.com/img923/7457/Mk6Y9K.jpg"));
  }

  private Long createFigurines(FigurineReq request) {
    ResponseEntity<FigurineResp> createResp =
        rest.postForEntity(FIGURINES, request, FigurineResp.class);

    assertThat(createResp.getStatusCode()).isEqualTo(CREATED);
    assertThat(createResp.getBody()).isNotNull();

    return createResp.getBody().id();
  }

  private Long createCatalog(CatalogType catalogType, CatalogReq request) {
    ResponseEntity<CatalogResp> response =
        rest.postForEntity("/catalogs/{type}", request, CatalogResp.class, catalogType.name());

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().getId();
  }

  private Long createDistributor(com.mesofi.mythclothapi.distributors.dto.DistributorReq request) {
    ResponseEntity<DistributorResp> response =
        rest.postForEntity("/distributors", request, DistributorResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }
}
