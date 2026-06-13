package com.mesofi.mythclothapi.catalogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.it.ControllerBaseIT;

/**
 * Integration tests for {@link CatalogController}.
 *
 * <p>Validates the complete lifecycle of catalog resources: creation, retrieval, and deletion
 * through the HTTP layer.
 *
 * <p>The test uses the real Spring context, security configuration, validation, persistence layer,
 * and REST endpoints.
 */
@Sql(scripts = "/cleanup-catalog-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CatalogControllerIT extends ControllerBaseIT {

  private static final String CATALOGS_ENDPOINT = "/catalogs";

  private static final List<CatalogTestData> CATALOGS =
      List.of(
          new CatalogTestData("/series", "The description Series"),
          new CatalogTestData("/groups", "The description Group"),
          new CatalogTestData("/distributions", "The description Distribution"),
          new CatalogTestData("/lineups", "The description LineUp"));

  /** Verifies the complete CRUD lifecycle for all supported catalog types. */
  @Test
  @DisplayName("Should create, read and delete catalog entries")
  void shouldManageCatalogLifecycle() {

    CATALOGS.forEach(
        catalog -> {
          Long id = createCatalog(catalog.resource(), catalog.request());

          assertThat(readCatalog(catalog.resource(), id)).isEqualTo(id);

          deleteCatalog(catalog.resource(), id);
        });
  }

  /**
   * Creates a new catalog entry.
   *
   * @param resource catalog endpoint resource
   * @param request catalog payload
   * @return created catalog identifier
   */
  private Long createCatalog(String resource, CatalogReq request) {

    ResponseEntity<CatalogResp> response =
        rest.post()
            .uri(CATALOGS_ENDPOINT + resource)
            .body(request)
            .retrieve()
            .toEntity(CatalogResp.class);

    assertThat(response.getStatusCode())
        .as("Catalog creation should return HTTP 201")
        .isEqualTo(CREATED);

    CatalogResp body = response.getBody();

    assertThat(body).as("Created catalog response should exist").isNotNull();

    return body.id();
  }

  /**
   * Retrieves an existing catalog entry.
   *
   * @param resource catalog endpoint resource
   * @param id catalog identifier
   * @return retrieved catalog identifier
   */
  private Long readCatalog(String resource, Long id) {

    ResponseEntity<CatalogResp> response =
        rest.get()
            .uri(CATALOGS_ENDPOINT + resource + "/" + id)
            .retrieve()
            .toEntity(CatalogResp.class);

    assertThat(response.getStatusCode())
        .as("Catalog retrieval should return HTTP 200")
        .isEqualTo(OK);

    CatalogResp body = response.getBody();

    assertThat(body).as("Catalog response should exist").isNotNull();

    assertThat(body.id()).as("Returned catalog id should match requested id").isEqualTo(id);

    return body.id();
  }

  /**
   * Deletes an existing catalog entry.
   *
   * @param resource catalog endpoint resource
   * @param id catalog identifier
   */
  private void deleteCatalog(String resource, Long id) {

    ResponseEntity<Void> response =
        rest.delete().uri(CATALOGS_ENDPOINT + resource + "/" + id).retrieve().toEntity(Void.class);

    assertThat(response.getStatusCode())
        .as("Catalog deletion should return HTTP 204")
        .isEqualTo(NO_CONTENT);
  }

  /**
   * Test data holder for catalog integration scenarios.
   *
   * @param resource API resource path
   * @param description catalog description
   */
  private record CatalogTestData(String resource, String description) {

    /**
     * Creates the catalog request payload.
     *
     * @return catalog creation request
     */
    CatalogReq request() {
      return new CatalogReq(description);
    }
  }
}
