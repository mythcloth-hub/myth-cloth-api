package com.mesofi.mythclothapi.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogType;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;

/**
 * Base class for all HTTP-based integration tests.
 *
 * <p>This class bootstraps the full Spring application context using the {@code test} profile and
 * exposes reusable helper methods for creating required domain data (catalogs, distributors, etc.)
 * through real HTTP calls.
 *
 * <p>All controller-level integration tests should extend this class to:
 *
 * <ul>
 *   <li>Ensure consistent environment configuration
 *   <li>Reuse common test setup logic
 *   <li>Validate real API behavior instead of mocking layers
 * </ul>
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

  /** REST client bound to the random test server port. */
  @Autowired protected TestRestTemplate rest;

  /** Base endpoint paths used across integration tests. */
  protected static final String FIGURINES = "/figurines";

  protected static final String CATALOGS = "/catalogs/{type}";
  protected static final String DISTRIBUTORS = "/distributors";
  protected static final String ANNIVERSARY = "/anniversaries";

  /**
   * Creates a catalog entry using the public API with optional extra attributes.
   *
   * @param type catalog type
   * @param description catalog description
   * @return the identifier of the created catalog
   */
  protected Long createCatalog(CatalogType type, String description) {
    ResponseEntity<CatalogResp> response =
        rest.postForEntity(CATALOGS, new CatalogReq(description), CatalogResp.class, type.name());

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }

  /**
   * Creates a distributor using the public API.
   *
   * <p>The request parameter is intentionally typed as {@link Object} to allow reuse across
   * different distributor request variants without coupling integration tests to a specific DTO.
   *
   * @param request distributor creation request payload
   * @return the identifier of the created distributor
   */
  protected Long createDistributor(Object request) {
    ResponseEntity<DistributorResp> response =
        rest.postForEntity(DISTRIBUTORS, request, DistributorResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }

  /**
   * Creates a new Anniversary catalog entry by invoking the Anniversary API endpoint.
   *
   * <p>This method sends the provided request payload using an HTTP POST request, validates that
   * the response status is {@code 201 CREATED}, ensures the response body is not {@code null}, and
   * returns the identifier of the created anniversary.
   *
   * @param request the request payload used to create the anniversary
   * @return the ID of the newly created anniversary
   * @throws AssertionError if the response status is not {@code CREATED} or the response body is
   *     {@code null}
   */
  protected Long createAnniversary(Object request) {
    ResponseEntity<AnniversaryResp> response =
        rest.postForEntity(ANNIVERSARY, request, AnniversaryResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }
}
