package com.mesofi.mythclothapi.references;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

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
  }

  private Long createReferences(String resource, ReferencePairRequest request) {
    ResponseEntity<ReferencePairResponse> createResp =
        rest.postForEntity(resource, request, ReferencePairResponse.class);

    assertThat(createResp.getStatusCode()).isEqualTo(CREATED);
    assertThat(createResp.getBody()).isNotNull();

    return createResp.getBody().id();
  }
}
