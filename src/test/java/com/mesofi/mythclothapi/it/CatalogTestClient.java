package com.mesofi.mythclothapi.it;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.MX;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.BANDAI;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.DAM;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.DTM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryReq;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogType;
import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;

@Component
@Profile("test")
public class CatalogTestClient {

  private final TestRestTemplate rest;

  /** Base endpoint path for catalog-related operations. */
  protected static final String CATALOGS = "/catalogs/{type}";

  /** Base endpoint path for distributor-related operations. */
  protected static final String DISTRIBUTORS = "/distributors";

  /** Base endpoint path for anniversary-related operations. */
  protected static final String ANNIVERSARY = "/anniversaries";

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
   * Creates catalogs for the given {@link CatalogType}.
   *
   * <p>The catalog names are predefined per catalog type and are created via real HTTP POST
   * requests.
   *
   * @param type the catalog type to create
   * @return a list of created {@link CatalogResp} objects
   */
  public List<CatalogResp> createCatalogs(CatalogType type) {

    List<String> names =
        switch (type) {
          case distributions -> List.of("Stores", "Tamashii Web Shop", "Tamashii Nations");
          case groups -> List.of("Bronze Saint V3", "Gold Saint", "Gold Inheritor");
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
   * Executes an HTTP POST request and asserts that the response status is {@code 201 CREATED}.
   *
   * <p>This helper method centralizes common assertions for entity creation endpoints and returns
   * the validated response body.
   *
   * @param url the endpoint URL
   * @param request the request payload
   * @param responseType the expected response type
   * @param uriVars optional URI variables
   * @param <T> the response body type
   * @return the non-null response body
   */
  protected <T> T postAndAssertCreated(
      String url, Object request, Class<T> responseType, Object... uriVars) {

    ResponseEntity<T> response = rest.postForEntity(url, request, responseType, uriVars);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody();
  }
}
