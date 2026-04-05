package com.mesofi.mythclothapi.figurineevents;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventReq;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurines.FigurineRepository;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service layer responsible for managing {@link FigurineEvent} records.
 *
 * <p>Provides CRUD operations for events associated with a specific figurine. Each event captures a
 * dated lifecycle milestone (e.g., pre-order, release, restock) linked to a {@link
 * com.mesofi.mythclothapi.figurines.model.Figurine}.
 *
 * <p>All write operations validate that the referenced figurine exists before persisting changes.
 * Read operations scope queries by both figurine ID and event ID to enforce ownership boundaries.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FigurineEventService {

  private final FigurineRepository figurineRepository;
  private final FigurineEventRepository repository;
  private final FigurineEventMapper mapper;

  /**
   * Creates a new event for the specified figurine.
   *
   * <p>Maps the incoming {@link FigurineEventReq} to a {@link FigurineEvent}, resolves and links
   * the associated {@link Figurine}, and persists the entity.
   *
   * @param request the event data to create; must not be {@code null} and must pass validation
   * @return the created event as a {@link FigurineEventResp}
   * @throws com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException if the figurine
   *     referenced by {@code request.figurineId} does not exist
   */
  @Transactional
  public FigurineEventResp createFigurineEvent(@NotNull @Valid FigurineEventReq request) {
    log.info(
        "Creating event for figurine {} - [{}]({})",
        request.getFigurineId(),
        request.getDate(),
        request.getDescription());

    FigurineEvent figurineEvent = mapper.toFigurineEvent(request);
    linkReferences(figurineEvent, request.getFigurineId());

    var saved = repository.save(figurineEvent);
    return mapper.toFigurineEventResp(saved);
  }

  private void linkReferences(FigurineEvent figurineEvent, long figurineId) {
    Figurine figurine =
        Optional.of(figurineId)
            .flatMap(figurineRepository::findById)
            .orElseThrow(() -> new FigurineNotFoundException(figurineId));

    figurineEvent.setFigurine(figurine);
  }

  /**
   * Retrieves a single event by its ID, scoped to the given figurine.
   *
   * @param figurineId the ID of the figurine that owns the event
   * @param eventId the ID of the event to retrieve
   * @return the matching event as a {@link FigurineEventResp}
   * @throws FigurineEventNotFoundException if no event with the given {@code eventId} exists for
   *     the specified figurine
   */
  @Transactional(readOnly = true)
  public FigurineEventResp retrieveFigurineEvent(@NotNull Long figurineId, @NotNull Long eventId) {
    return repository
        .findByIdAndFigurineId(eventId, figurineId)
        .map(mapper::toFigurineEventResp)
        .orElseThrow(() -> new FigurineEventNotFoundException(eventId));
  }

  /**
   * Retrieves all events associated with the given figurine.
   *
   * @param figurineId the ID of the figurine whose events are to be retrieved
   * @return a non-empty list of events as {@link FigurineEventResp}
   * @throws com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException if no events are
   *     found for the specified figurine, treating this as an indication that the figurine does not
   *     exist or has no recorded events
   */
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

  /**
   * Updates an existing event belonging to the specified figurine.
   *
   * <p>Looks up the event by both {@code eventId} and {@code figurineId} to enforce ownership. If
   * the incoming request carries a different figurine ID, the event is reassigned to that figurine
   * after verifying it exists.
   *
   * @param figurineId the ID of the figurine that currently owns the event
   * @param eventId the ID of the event to update
   * @param newRequest the updated event data; must pass validation
   * @return the updated event as a {@link FigurineEventResp}
   * @throws FigurineEventNotFoundException if no event with the given {@code eventId} exists for
   *     the specified figurine
   * @throws com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException if the figurine
   *     referenced by {@code newRequest.figurineId} does not exist
   */
  @Transactional
  public FigurineEventResp updateFigurineEvent(
      @NotNull Long figurineId, @NotNull Long eventId, @Valid FigurineEventReq newRequest) {
    log.info(
        "Updating event '{}' for figurine {} to [{}]({}) - {}",
        eventId,
        figurineId,
        newRequest.getDate(),
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

    figurineEvent.setEventDate(newRequest.getDate());
    figurineEvent.setDescription(newRequest.getDescription());
    if (!Objects.equals(figurine.getId(), figurineId)) {
      // there was a change ... the new figureId is different from the existing one.
      figurineEvent.setFigurine(figurine);
    }
    var updated = repository.save(figurineEvent);
    return mapper.toFigurineEventResp(updated);
  }

  /**
   * Removes an event belonging to the specified figurine.
   *
   * <p>Looks up the event by both {@code eventId} and {@code figurineId} to enforce ownership
   * before deleting it.
   *
   * @param figurineId the ID of the figurine that owns the event
   * @param eventId the ID of the event to delete
   * @throws FigurineEventNotFoundException if no event with the given {@code eventId} exists for
   *     the specified figurine
   */
  public void removeFigurineEvent(@NotNull Long figurineId, @NotNull Long eventId) {
    log.warn("Removing event: {} for figurine: {}", eventId, figurineId);

    FigurineEvent figurineEvent =
        repository
            .findByIdAndFigurineId(eventId, figurineId)
            .orElseThrow(() -> new FigurineEventNotFoundException(eventId));

    repository.delete(figurineEvent);
  }
}
