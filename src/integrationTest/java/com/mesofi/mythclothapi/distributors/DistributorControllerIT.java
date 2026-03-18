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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.Distributor;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DistributorControllerIT {

  private static final String DISTRIBUTORS = "/distributors";

  private final RestClient rest;

  public DistributorControllerIT(@Value("${local.server.port}") int port) {
    this.rest = RestClient.builder().baseUrl("http://localhost:" + port).build();
  }

  @Test
  @DisplayName("Test flow to create and process distributors")
  void fullCrudDistributorFlow() {

    DistributorReq reqBandai = new DistributorReq(BANDAI, JP, "https://tamashiiweb.com/");
    DistributorReq reqDam = new DistributorReq(DAM, MX, "https://animexico-online.com/");

    // CREATE
    Long idBandai = createDistributor(reqBandai);
    Long idDam = createDistributor(reqDam);

    // READ
    assertThat(readDistributor(idBandai)).isEqualTo(idBandai);
    assertThat(readDistributor(idDam)).isEqualTo(idDam);

    // READ ALL
    readAll();

    // UPDATE
    updateDistributor(idBandai, new DistributorReq(BANDAI, MX, "https://tamashiiweb.com/"));

    // VERIFY UPDATE
    assertThat(readDistributor(idBandai)).isEqualTo(idBandai);

    // DELETE
    deleteDistributor(idBandai);
    deleteDistributor(idDam);
  }

  private void updateDistributor(Long id, DistributorReq request) {
    rest.put().uri(DISTRIBUTORS + "/" + id).body(request).retrieve().toBodilessEntity();
  }

  private Long createDistributor(DistributorReq request) {
    ResponseEntity<DistributorResp> response =
        rest.post().uri(DISTRIBUTORS).body(request).retrieve().toEntity(DistributorResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }

  private Long readDistributor(Long id) {
    ResponseEntity<DistributorResp> response =
        rest.get().uri(DISTRIBUTORS + "/" + id).retrieve().toEntity(DistributorResp.class);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(id);

    return response.getBody().id();
  }

  private void readAll() {
    ResponseEntity<Distributor[]> response =
        rest.get().uri(DISTRIBUTORS).retrieve().toEntity(Distributor[].class);

    assertThat(response.getBody()).hasSize(2);
  }

  private void deleteDistributor(Long id) {
    rest.delete().uri(DISTRIBUTORS + "/" + id).retrieve().toBodilessEntity();
  }
}
