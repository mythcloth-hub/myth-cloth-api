package com.mesofi.mythclothapi.it;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.MX;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.BANDAI;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.DAM;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.DTM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryReq;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogType;
import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;

/**
 * Test-only client used for creating and cleaning up catalog-related data through real HTTP calls.
 *
 * <p>This component is active only under the {@code test} Spring profile and is intended to support
 * integration tests by:
 *
 * <ul>
 *   <li>Creating predefined distributors, catalogs, and anniversaries
 *   <li>Asserting expected HTTP response statuses
 *   <li>Encapsulating repetitive REST interaction logic
 * </ul>
 *
 * <p>This class deliberately fails fast using assertions to ensure tests stop immediately when the
 * system under test behaves unexpectedly.
 */
@Component
@Profile("test")
public class CatalogTestClient {

  /** Spring REST client configured for integration testing. */
  private final TestRestTemplate rest;

  /** Base endpoint path for catalog-related operations. */
  protected static final String CATALOGS = "/catalogs/{type}";

  /** Endpoint path for deleting a catalog by type and ID. */
  protected static final String CATALOGS_DELETE = CATALOGS + "/{id}";

  /** Base endpoint path for distributor-related operations. */
  protected static final String DISTRIBUTORS = "/distributors";

  /** Endpoint path for deleting a distributor by ID. */
  protected static final String DISTRIBUTORS_DELETE = DISTRIBUTORS + "/{id}";

  /** Base endpoint path for anniversary-related operations. */
  protected static final String ANNIVERSARY = "/anniversaries";

  /** Endpoint path for deleting an anniversary by ID. */
  protected static final String ANNIVERSARY_DELETE = ANNIVERSARY + "/{id}";

  /**
   * Creates a new {@code CatalogTestClient}.
   *
   * @param rest configured {@link TestRestTemplate} used to execute HTTP calls
   */
  public CatalogTestClient(TestRestTemplate rest) {
    this.rest = rest;
  }

  /**
   * Creates a predefined set of distributors by invoking the distributors API.
   *
   * <p>Each distributor is persisted via a real HTTP POST request and validated to return {@link
   * org.springframework.http.HttpStatus#CREATED}.
   *
   * @return a list of created {@link DistributorResp} objects
   */
  public List<DistributorResp> createDistributors() {
    return Stream.of(
            new DistributorReq(BANDAI, JP, "https://tamashii.jp/"),
            new DistributorReq(DAM, MX, "https://animexico-online.com/"),
            new DistributorReq(DTM, MX, null))
        .map(req -> postAndAssertCreated(DISTRIBUTORS, req, DistributorResp.class))
        .toList();
  }

  /**
   * Deletes a distributor by ID and asserts a {@code 204 NO_CONTENT} response.
   *
   * @param id the distributor identifier
   */
  public void deleteDistributor(long id) {
    deleteAndAssertNoContent(DISTRIBUTORS_DELETE, id);
  }

  /**
   * Creates catalogs for the given {@link CatalogType}.
   *
   * <p>The catalog descriptions are predefined per catalog type and are created using real HTTP
   * POST requests.
   *
   * @param type the catalog type to create
   * @return a list of created {@link CatalogResp} objects
   */
  public List<CatalogResp> createCatalogs(CatalogType type) {

    List<String> names =
        switch (type) {
          case distributions -> List.of("Stores", "Tamashii Web Shop", "Tamashii Nations");
          case groups ->
              List.of(
                  "Bronze Saint V2",
                  "Bronze Saint V3",
                  "Bronze Saint V4",
                  "Gold Saint",
                  "Gold Inheritor");
          case series -> List.of("Saint Seiya", "Saintia Sho", "Soul of Gold");
          case lineups -> List.of("Myth Cloth EX", "Myth Cloth", "Appendix");
        };

    return names.stream()
        .map(
            description ->
                postAndAssertCreated(
                    CATALOGS, new CatalogReq(description), CatalogResp.class, type.name()))
        .toList();
  }

  /**
   * Deletes a catalog entry by type and ID and asserts a {@code 204 NO_CONTENT} response.
   *
   * @param catalogType the catalog type
   * @param id the catalog identifier
   */
  public void deleteCatalog(CatalogType catalogType, long id) {
    deleteAndAssertNoContent(CATALOGS_DELETE, catalogType, id);
  }

  /**
   * Creates a predefined set of anniversaries by invoking the anniversaries API.
   *
   * @return a list of created {@link AnniversaryResp} objects
   */
  public List<AnniversaryResp> createAnniversaries() {
    return Stream.of(
            new AnniversaryReq("Masami Kurumada 40th Anniversary", 40),
            new AnniversaryReq("20th Anniversary", 20))
        .map(req -> postAndAssertCreated(ANNIVERSARY, req, AnniversaryResp.class))
        .toList();
  }

  /**
   * Deletes an anniversary by ID and asserts a {@code 204 NO_CONTENT} response.
   *
   * @param id the anniversary identifier
   */
  public void deleteAnniversary(long id) {
    deleteAndAssertNoContent(ANNIVERSARY_DELETE, id);
  }

  /**
   * Executes an HTTP POST request and asserts that the response status is {@code 201 CREATED}.
   *
   * <p>This helper centralizes creation assertions for test readability and consistency.
   *
   * @param url the endpoint URL
   * @param request the request payload
   * @param responseType the expected response type
   * @param uriVars optional URI variables
   * @param <T> the response body type
   * @return the non-null response body
   */
  private <T> T postAndAssertCreated(
      String url, Object request, Class<T> responseType, Object... uriVars) {

    ResponseEntity<T> response = rest.postForEntity(url, request, responseType, uriVars);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody();
  }

  /**
   * Executes an HTTP DELETE request and asserts that the response status is {@code 204 NO_CONTENT}.
   *
   * @param url the endpoint URL
   * @param uriVars optional URI variables
   */
  private void deleteAndAssertNoContent(String url, Object... uriVars) {
    ResponseEntity<Void> response =
        rest.exchange(url, HttpMethod.DELETE, null, Void.class, uriVars);

    assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
  }
}
