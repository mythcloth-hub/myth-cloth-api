package com.mesofi.mythclothapi.it;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.CN;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.MX;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.BANDAI;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.BANDAI_CHINA;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.DAM;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.DTM;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.web.client.RestClient;

import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;

public class CatalogTestClient {
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

  private final RestClient rest;

  public CatalogTestClient(RestClient rest) {
    this.rest = rest;
  }

  public List<DistributorResp> createDistributors() {
    return Stream.of(
            new DistributorReq(BANDAI, JP, "https://tamashii.jp/"),
            new DistributorReq(DAM, MX, "https://animexico-online.com/"),
            new DistributorReq(DTM, MX, null),
            new DistributorReq(BANDAI_CHINA, CN, null))
        .map(req -> postAndAssertCreated(DISTRIBUTORS, req, DistributorResp.class))
        .toList();
  }

  private <T> T postAndAssertCreated(
      String url, Object request, Class<T> responseType, Object... uriVars) {
    /*
           ResponseEntity<T> response = rest.postForEntity(url, request, responseType, uriVars);
           assertThat(response.getStatusCode()).isEqualTo(CREATED);
           assertThat(response.getBody()).isNotNull();
           return response.getBody();
    */
    return null;
  }
}
