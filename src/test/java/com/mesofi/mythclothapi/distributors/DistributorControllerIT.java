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

import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.Distributor;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DistributorControllerIT {

  @Autowired private TestRestTemplate rest;

  private final String DISTRIBUTORS = "/distributors";

  @Test
  @DisplayName("Test flow to create and process distributors")
  void fullCrudDistributorFlow() {
    DistributorReq reqBandai = new DistributorReq(BANDAI, JP, "https://tamashiiweb.com/");
    DistributorReq reqDam = new DistributorReq(DAM, MX, "https://animexico-online.com/");

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
        idBandai, new DistributorReq(BANDAI, MX, "https://tamashiiweb.com/")); // JP -> MX

    // READ UPDATED (find by id)
    assertThat(readDistributor(idBandai)).isEqualTo(1);
  }

  private void updateDistributor(Long existingId, DistributorReq newRequest) {
    rest.put(DISTRIBUTORS + "/" + existingId, newRequest);
  }

  private Long createDistributor(DistributorReq request) {
    ResponseEntity<DistributorResp> createResp =
        rest.postForEntity(DISTRIBUTORS, request, DistributorResp.class);

    assertThat(createResp.getStatusCode()).isEqualTo(CREATED);
    assertThat(createResp.getBody()).isNotNull();

    return createResp.getBody().id();
  }

  private Long readDistributor(Long id) {
    ResponseEntity<DistributorResp> createResp =
        rest.getForEntity(DISTRIBUTORS + "/" + id, DistributorResp.class);

    assertThat(createResp.getStatusCode()).isEqualTo(OK);
    assertThat(createResp.getBody()).isNotNull();
    assertThat(createResp.getBody().id()).isEqualTo(id);

    return createResp.getBody().id();
  }

  private void readAll() {
    ResponseEntity<Distributor[]> findAllResp =
        rest.getForEntity(DISTRIBUTORS, Distributor[].class);
    assertThat(findAllResp.getBody()).hasSize(2);
  }
}
