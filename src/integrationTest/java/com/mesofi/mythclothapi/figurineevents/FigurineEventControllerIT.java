package com.mesofi.mythclothapi.figurineevents;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.BANDAI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventReq;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;

@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class FigurineEventControllerIT {

  private static final String LINE_UP = "/catalogs/lineups";
  private static final String LINE_UP_DEL = LINE_UP + "/{id}";
  private static final String SERIES = "/catalogs/series";
  private static final String SERIES_DEL = SERIES + "/{id}";
  private static final String GROUPS = "/catalogs/groups";
  private static final String GROUPS_DEL = GROUPS + "/{id}";

  private static final String DISTRIBUTORS = "/distributors";
  private static final String DISTRIBUTORS_DEL = DISTRIBUTORS + "/{id}";

  private static final String FIGURINES = "/figurines";
  private static final String FIGURINES_DEL = FIGURINES + "/{id}";

  private static final String EVENTS = "/figurines/{figurineId}/events";
  private static final String EVENTS_DEL = EVENTS + "/{id}";

  private final RestClient rest;

  public FigurineEventControllerIT(
      @Value("${local.server.port}") int port,
      @Value("${server.servlet.context-path}") String contextPath) {

    this.rest = RestClient.builder().baseUrl("http://localhost:" + port + contextPath).build();
  }

  @Test
  @DisplayName("Test flow to create and process distributors")
  void createPrototypeFigurine_returnsCreated() {

    Long lineUpId = createCatalog(LINE_UP, "Myth Cloth");
    Long seriesId = createCatalog(SERIES, "Saint Seiya");
    Long groupId = createCatalog(GROUPS, "Bronze Saint V2");
    Long distributorId = createDistributor();

    com.mesofi.mythclothapi.figurines.dto.DistributorReq distributorReq =
        new com.mesofi.mythclothapi.figurines.dto.DistributorReq(
            distributorId, CurrencyCode.JPY, 13000d, null, null, null, null);

    FigurineReq figurineReq = createBasicFigurine(distributorReq, lineUpId, seriesId, groupId);
    Long figurineId = createFigurine(figurineReq);

    Long eventId = createEvent(figurineId);

    removeResource(EVENTS_DEL, figurineId, eventId);
    removeResource(FIGURINES_DEL, figurineId);
    removeResource(DISTRIBUTORS_DEL, distributorId);
    removeResource(GROUPS_DEL, groupId);
    removeResource(SERIES_DEL, seriesId);
    removeResource(LINE_UP_DEL, lineUpId);
  }

  private Long createEvent(Long figurineId) {
    FigurineEventReq figurineEventReq = new FigurineEventReq();
    figurineEventReq.setDescription("Event description");
    figurineEventReq.setEventDate(LocalDate.of(2024, 6, 1));
    figurineEventReq.setRegion(CountryCode.MX);
    figurineEventReq.setType(FigurineEventType.ANNOUNCEMENT);
    figurineEventReq.setFigurineId(figurineId);

    ResponseEntity<FigurineEventResp> response =
        rest.post()
            .uri(EVENTS, figurineId)
            .body(figurineEventReq)
            .retrieve()
            .toEntity(FigurineEventResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }

  private Long createCatalog(String catalogUrl, String description) {
    CatalogReq catalogReq = new CatalogReq(description);
    ResponseEntity<CatalogResp> response =
        rest.post().uri(catalogUrl).body(catalogReq).retrieve().toEntity(CatalogResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }

  private Long createDistributor() {
    DistributorReq distributorReq = new DistributorReq(BANDAI, JP, "https://tamashiiweb.com/");

    ResponseEntity<DistributorResp> response =
        rest.post()
            .uri(DISTRIBUTORS)
            .body(distributorReq)
            .retrieve()
            .toEntity(DistributorResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }

  private Long createFigurine(FigurineReq request) {
    ResponseEntity<FigurineResp> response =
        rest.post().uri(FIGURINES).body(request).retrieve().toEntity(FigurineResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }

  private FigurineReq createBasicFigurine(
      com.mesofi.mythclothapi.figurines.dto.DistributorReq distributorReq,
      Long lineUpId,
      Long seriesId,
      Long groupId) {
    return new FigurineReq(
        "Pegasus Seiya",
        List.of(distributorReq),
        null,
        null,
        lineUpId,
        seriesId,
        groupId,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }

  private void removeResource(String uri, @Nullable Object... uriVariables) {
    rest.delete().uri(uri, uriVariables).retrieve().toBodilessEntity();
  }
}
