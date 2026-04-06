package com.mesofi.mythclothapi.figurineevents;

import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.ANNOUNCEMENT;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.PREORDER_OPEN;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.RELEASE;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.DISTRIBUTORS_DEL;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.FIGURINES_DEL;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.GROUPS_DEL;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.LINE_UP_DEL;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.SERIES_DEL;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.createBasicFigurine;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.removeResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventReq;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.it.ControllerBaseIT;
import com.mesofi.mythclothapi.utils.FigurineIdentifiers;

public class FigurineEventControllerIT extends ControllerBaseIT {

  private static final String EVENTS_BY_FIGURINE = "/figurines/{figurineId}/events";
  private static final String EVENTS_BY_ID = EVENTS_BY_FIGURINE + "/{id}";

  @Test
  @DisplayName("Test flow to create and process figurine events")
  void fullCrudFigurineEventFlow() {

    FigurineIdentifiers figIds = createBasicFigurine(rest);

    // CREATE
    Long eventId1 = createFigurineEvent(figIds.id(), "First appearance on events", ANNOUNCEMENT);
    Long eventId2 = createFigurineEvent(figIds.id(), "Preorders open", PREORDER_OPEN);
    Long eventId3 = createFigurineEvent(figIds.id(), "Released", RELEASE);

    // READ
    assertThat(readFigurineEvent(figIds.id(), eventId1)).isEqualTo(eventId1);
    assertThat(readFigurineEvent(figIds.id(), eventId2)).isEqualTo(eventId2);
    assertThat(readFigurineEvent(figIds.id(), eventId3)).isEqualTo(eventId3);

    // READ ALL
    readAll(figIds.id());

    // UPDATE
    FigurineEventReq toUpdateFigurineEventReq =
        createFigurineEventReq(figIds.id(), "New Event", ANNOUNCEMENT);
    updateFigurineEvent(figIds.id(), eventId2, toUpdateFigurineEventReq);

    // DELETE
    removeResource(rest, EVENTS_BY_ID, figIds.id(), eventId1);
    removeResource(rest, EVENTS_BY_ID, figIds.id(), eventId2);
    removeResource(rest, EVENTS_BY_ID, figIds.id(), eventId3);

    removeResource(rest, FIGURINES_DEL, figIds.id());
    removeResource(rest, DISTRIBUTORS_DEL, figIds.distributorId());
    removeResource(rest, GROUPS_DEL, figIds.groupId());
    removeResource(rest, SERIES_DEL, figIds.seriesId());
    removeResource(rest, LINE_UP_DEL, figIds.lineUpId());
  }

  private void updateFigurineEvent(
      Long figurineId, Long id, FigurineEventReq toUpdateFigurineEventReq) {
    ResponseEntity<FigurineEventResp> response =
        rest.put()
            .uri(EVENTS_BY_ID, figurineId, id)
            .body(toUpdateFigurineEventReq)
            .retrieve()
            .toEntity(FigurineEventResp.class);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(id);
    assertThat(response.getBody().description().equals(toUpdateFigurineEventReq.getDescription()));
  }

  private void readAll(Long id) {
    ResponseEntity<FigurineEventResp[]> response =
        rest.get().uri(EVENTS_BY_FIGURINE, id).retrieve().toEntity(FigurineEventResp[].class);

    assertThat(response.getBody()).hasSize(3);
  }

  private Long readFigurineEvent(Long figurineId, Long id) {
    ResponseEntity<FigurineEventResp> response =
        rest.get().uri(EVENTS_BY_ID, figurineId, id).retrieve().toEntity(FigurineEventResp.class);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(id);

    return response.getBody().id();
  }

  private Long createFigurineEvent(Long figurineId, String description, FigurineEventType type) {
    FigurineEventReq figurineEventReq = createFigurineEventReq(figurineId, description, type);

    ResponseEntity<FigurineEventResp> response =
        rest.post()
            .uri(EVENTS_BY_FIGURINE, figurineId)
            .body(figurineEventReq)
            .retrieve()
            .toEntity(FigurineEventResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().id();
  }

  private FigurineEventReq createFigurineEventReq(
      Long figurineId, String description, FigurineEventType type) {
    FigurineEventReq figurineEventReq = new FigurineEventReq();
    figurineEventReq.setDescription(description);
    figurineEventReq.setDate(LocalDate.of(2024, 6, 1));
    figurineEventReq.setRegion(CountryCode.MX);
    figurineEventReq.setType(type);
    figurineEventReq.setFigurineId(figurineId);

    return figurineEventReq;
  }
}
