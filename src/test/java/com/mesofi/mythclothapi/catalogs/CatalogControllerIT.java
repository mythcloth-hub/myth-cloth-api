package com.mesofi.mythclothapi.catalogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CatalogControllerIT {

  @Autowired private TestRestTemplate rest;

  private final String CATALOG = "/catalogs";
  private final String SERIES = "/series";
  private final String GROUP = "/groups";
  private final String DISTRIBUTION = "/distributions";
  private final String LINE_UP = "/lineups";

  @Test
  @DisplayName("Test flow to create and process catalogs")
  void fullCrudCatalogFlow() {
    CatalogReq reqSeries = new CatalogReq("The description Series");
    CatalogReq reqGroup = new CatalogReq("The description Group");
    CatalogReq reqDistribution = new CatalogReq("The description Distribution");
    CatalogReq reqLineUp = new CatalogReq("The description LineUp");

    // CREATE
    Long idSeries = createReferences(SERIES, reqSeries);
    Long idGroup = createReferences(GROUP, reqGroup);
    Long idDistribution = createReferences(DISTRIBUTION, reqDistribution);
    Long idLineUp = createReferences(LINE_UP, reqLineUp);

    // READ (find by id)
    assertThat(readDistributor(SERIES, idSeries)).isEqualTo(1);
    assertThat(readDistributor(GROUP, idGroup)).isEqualTo(1);
    assertThat(readDistributor(DISTRIBUTION, idDistribution)).isEqualTo(1);
    assertThat(readDistributor(LINE_UP, idLineUp)).isEqualTo(1);
  }

  private Long createReferences(String resource, CatalogReq request) {
    ResponseEntity<CatalogResp> createResp =
        rest.postForEntity(CATALOG + resource, request, CatalogResp.class);

    assertThat(createResp.getStatusCode()).isEqualTo(CREATED);
    assertThat(createResp.getBody()).isNotNull();

    return createResp.getBody().id();
  }

  private Long readDistributor(String resource, Long id) {
    ResponseEntity<CatalogResp> createResp =
        rest.getForEntity(CATALOG + resource + "/" + id, CatalogResp.class);

    assertThat(createResp.getStatusCode()).isEqualTo(OK);
    assertThat(createResp.getBody()).isNotNull();
    assertThat(createResp.getBody().id()).isEqualTo(id);

    return createResp.getBody().id();
  }
}
