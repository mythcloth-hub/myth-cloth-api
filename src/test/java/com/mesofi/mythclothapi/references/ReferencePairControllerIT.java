package com.mesofi.mythclothapi.references;

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

import com.mesofi.mythclothapi.references.model.ReferencePairRequest;
import com.mesofi.mythclothapi.references.model.ReferencePairResponse;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReferencePairControllerIT {

  @Autowired private TestRestTemplate rest;

  private final String REF = "/ref";
  private final String SERIES = "/series";
  private final String GROUP = "/groups";
  private final String DISTRIBUTION = "/distributions";
  private final String LINE_UP = "/lineups";

  @Test
  @DisplayName("Test flow to create and process references")
  void fullCrudReferencesFlow() {
    ReferencePairRequest reqSeries = new ReferencePairRequest("The description Series");
    ReferencePairRequest reqGroup = new ReferencePairRequest("The description Group");
    ReferencePairRequest reqDistribution = new ReferencePairRequest("The description Distribution");
    ReferencePairRequest reqLineUp = new ReferencePairRequest("The description LineUp");

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

  private Long createReferences(String resource, ReferencePairRequest request) {
    ResponseEntity<ReferencePairResponse> createResp =
        rest.postForEntity(REF + resource, request, ReferencePairResponse.class);

    assertThat(createResp.getStatusCode()).isEqualTo(CREATED);
    assertThat(createResp.getBody()).isNotNull();

    return createResp.getBody().id();
  }

  private Long readDistributor(String resource, Long id) {
    ResponseEntity<ReferencePairResponse> createResp =
        rest.getForEntity(REF + resource + "/" + id, ReferencePairResponse.class);

    assertThat(createResp.getStatusCode()).isEqualTo(OK);
    assertThat(createResp.getBody()).isNotNull();
    assertThat(createResp.getBody().id()).isEqualTo(id);

    return createResp.getBody().id();
  }
}
