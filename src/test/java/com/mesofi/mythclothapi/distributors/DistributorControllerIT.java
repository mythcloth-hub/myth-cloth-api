package com.mesofi.mythclothapi.distributors;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.MX;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.BANDAI;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.DAM;
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

import com.mesofi.mythclothapi.distributors.model.DistributorRequest;
import com.mesofi.mythclothapi.distributors.model.DistributorResponse;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DistributorControllerIT {

  @Autowired private TestRestTemplate rest;

  private final String DISTRIBUTORS = "/distributors";

  @Test
  @DisplayName("Test flow to create and process distributors")
  void fullCrudDistributorFlow() {
    DistributorRequest reqBandai = new DistributorRequest(BANDAI, JP, "https://tamashiiweb.com/");
    DistributorRequest reqDam = new DistributorRequest(DAM, MX, "https://animexico-online.com/");

    // CREATE
    Long idBandai = createDistributor(reqBandai);
    Long idDam = createDistributor(reqDam);

    // READ (find by id)
    assertThat(readDistributor(idBandai)).isEqualTo(1);
    assertThat(readDistributor(idDam)).isEqualTo(2);

    // READ ALL
    readAll();

    // UPDATE
    updateDistributor(
        idBandai, new DistributorRequest(BANDAI, MX, "https://tamashiiweb.com/")); // JP -> MX

    // READ UPDATED (find by id)
    assertThat(readDistributor(idBandai)).isEqualTo(1);
  }

  private void updateDistributor(Long existingId, DistributorRequest newRequest) {
    rest.put(DISTRIBUTORS + "/" + existingId, newRequest);
  }

  private Long createDistributor(DistributorRequest request) {
    ResponseEntity<DistributorResponse> createResp =
        rest.postForEntity(DISTRIBUTORS, request, DistributorResponse.class);

    assertThat(createResp.getStatusCode()).isEqualTo(CREATED);
    assertThat(createResp.getBody()).isNotNull();

    return createResp.getBody().id();
  }

  private Long readDistributor(Long id) {
    ResponseEntity<DistributorResponse> createResp =
        rest.getForEntity(DISTRIBUTORS + "/" + id, DistributorResponse.class);

    assertThat(createResp.getStatusCode()).isEqualTo(OK);
    assertThat(createResp.getBody()).isNotNull();
    assertThat(createResp.getBody().id()).isEqualTo(id);

    return createResp.getBody().id();
  }

  private void readAll() {
    ResponseEntity<DistributorEntity[]> findAllResp =
        rest.getForEntity(DISTRIBUTORS, DistributorEntity[].class);
    assertThat(findAllResp.getBody()).hasSize(2);
  }
}
