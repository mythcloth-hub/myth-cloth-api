package com.mesofi.mythclothapi.catalogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;

@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CatalogControllerIT {

  private final String CATALOG = "/catalogs";
  private final String SERIES = "/series";
  private final String GROUP = "/groups";
  private final String DISTRIBUTION = "/distributions";
  private final String LINE_UP = "/lineups";

  private final RestClient rest;

  // Inject random port and build RestClient once
  public CatalogControllerIT(@Value("${local.server.port}") int port) {
    this.rest = RestClient.builder().baseUrl("http://localhost:" + port).build();
  }

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

    // READ
    assertThat(readCatalog(SERIES, idSeries)).isEqualTo(idSeries);
    assertThat(readCatalog(GROUP, idGroup)).isEqualTo(idGroup);
    assertThat(readCatalog(DISTRIBUTION, idDistribution)).isEqualTo(idDistribution);
    assertThat(readCatalog(LINE_UP, idLineUp)).isEqualTo(idLineUp);

    // DELETE
    deleteCatalog(SERIES, idSeries);
    deleteCatalog(GROUP, idGroup);
    deleteCatalog(DISTRIBUTION, idDistribution);
    deleteCatalog(LINE_UP, idLineUp);
  }

  private Long createReferences(String resource, CatalogReq request) {

    ResponseEntity<CatalogResp> response =
        rest.post().uri(CATALOG + resource).body(request).retrieve().toEntity(CatalogResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }

  private Long readCatalog(String resource, Long id) {

    ResponseEntity<CatalogResp> response =
        rest.get().uri(CATALOG + resource + "/" + id).retrieve().toEntity(CatalogResp.class);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(id);

    return response.getBody().id();
  }

  private void deleteCatalog(String resource, Long id) {

    ResponseEntity<Void> response =
        rest.delete().uri(CATALOG + resource + "/" + id).retrieve().toEntity(Void.class);

    assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
  }
}
