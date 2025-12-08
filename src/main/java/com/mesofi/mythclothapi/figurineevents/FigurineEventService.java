package com.mesofi.mythclothapi.figurineevents;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventReq;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurines.FigurineRepository;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FigurineEventService {

  private final FigurineRepository figurineRepository;
  private final FigurineEventRepository repository;
  private final FigurineEventMapper mapper;

  @Transactional
  public FigurineEventResp createFigurineEvent(@NotNull @Valid FigurineEventReq request) {
    log.info(
        "Creating event for figurine {} - [{}]({})",
        request.getFigurineId(),
        request.getEventDate(),
        request.getDescription());

    Figurine figurine =
        Optional.of(request.getFigurineId())
            .flatMap(figurineRepository::findById)
            .orElseThrow(() -> new FigurineNotFoundException(request.getFigurineId()));

    FigurineEvent figurineEvent = mapper.toFigurineEvent(request);
    figurineEvent.setFigurine(figurine);
    var saved = repository.save(figurineEvent);
    return mapper.toFigurineEventResp(saved);
  }

  @Transactional(readOnly = true)
  public FigurineEventResp retrieveFigurineEvent(@NotNull Long figurineId, @NotNull Long eventId) {
    return repository
        .findByIdAndFigurineId(eventId, figurineId)
        .map(mapper::toFigurineEventResp)
        .orElseThrow(() -> new FigurineEventNotFoundException(eventId));
  }

  @Transactional(readOnly = true)
  public List<FigurineEventResp> retrieveFigurineEvents(@NotNull Long figurineId) {
    return Optional.of(figurineId)
        .map(repository::findAllByFigurineId)
        .filter(list -> !list.isEmpty())
        .orElseThrow(() -> new FigurineNotFoundException(figurineId))
        .stream()
        .map(mapper::toFigurineEventResp)
        .toList();
  }

  @Transactional
  public FigurineEventResp updateFigurineEvent(
      @NotNull Long figurineId, @NotNull Long eventId, @Valid FigurineEventReq newRequest) {
    log.info(
        "Updating event '{}' for figurine {} to [{}]({}) - {}",
        eventId,
        figurineId,
        newRequest.getEventDate(),
        newRequest.getFigurineId(),
        newRequest.getDescription());

    FigurineEvent figurineEvent =
        repository
            .findByIdAndFigurineId(eventId, figurineId)
            .orElseThrow(() -> new FigurineEventNotFoundException(eventId));

    // Ensure the new figurine ID exists, as the event may be reassigned to another figurine.
    Figurine figurine =
        Optional.of(newRequest.getFigurineId())
            .flatMap(figurineRepository::findById)
            .orElseThrow(() -> new FigurineNotFoundException(newRequest.getFigurineId()));

    figurineEvent.setEventDate(newRequest.getEventDate());
    figurineEvent.setDescription(newRequest.getDescription());
    if (!Objects.equals(figurine.getId(), figurineId)) {
      // there was a change ... the new figureId is different from the existing one.
      figurineEvent.setFigurine(figurine);
    }
    var updated = repository.save(figurineEvent);
    return mapper.toFigurineEventResp(updated);
  }

  public void removeFigurineEvent(@NotNull Long figurineId, @NotNull Long eventId) {
    log.warn("Removing event: {} for figurine: {}", eventId, figurineId);

    FigurineEvent figurineEvent =
        repository
            .findByIdAndFigurineId(eventId, figurineId)
            .orElseThrow(() -> new FigurineEventNotFoundException(eventId));

    repository.delete(figurineEvent);
  }
}
