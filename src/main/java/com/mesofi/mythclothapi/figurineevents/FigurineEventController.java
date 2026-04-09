package com.mesofi.mythclothapi.figurineevents;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventReq;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/figurines/{figurineId}/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FigurineEventController {

  private final FigurineEventService service;

  @PostMapping
  public ResponseEntity<FigurineEventResp> createEvent(
      @Positive @PathVariable Long figurineId,
      @Valid @RequestBody FigurineEventReq figurineEventRequest) {
    figurineEventRequest.setFigurineId(figurineId);
    FigurineEventResp response = service.createFigurineEvent(figurineEventRequest);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest() // e.g. /api/distributors
            .path("/figurines/{figurineId}/events/{id}") // append /{id}
            .buildAndExpand(figurineId, response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  public FigurineEventResp retrieveEvent(
      @Positive @PathVariable Long figurineId, @PathVariable Long id) {
    return service.retrieveFigurineEvent(figurineId, id);
  }

  @GetMapping
  public List<FigurineEventResp> retrieveEvents(@Positive @PathVariable Long figurineId) {
    return service.retrieveFigurineEvents(figurineId);
  }

  @PutMapping("/{id}")
  public ResponseEntity<FigurineEventResp> updateEvent(
      @Positive @PathVariable Long figurineId,
      @PathVariable Long id,
      @Valid @RequestBody FigurineEventReq figurineEventRequest) {
    FigurineEventResp updated = service.updateFigurineEvent(figurineId, id, figurineEventRequest);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> removeEvent(
      @Positive @PathVariable Long figurineId, @PathVariable Long id) {
    service.removeFigurineEvent(figurineId, id);
    return ResponseEntity.noContent().build();
  }
}
