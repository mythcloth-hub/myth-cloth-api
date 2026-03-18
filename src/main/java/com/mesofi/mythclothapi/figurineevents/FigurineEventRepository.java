package com.mesofi.mythclothapi.figurineevents;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;

/**
 * Repository interface for managing {@link FigurineEvent} entities.
 *
 * <p>Provides query methods to retrieve figurine events by their associated figurine or by a
 * combination of event ID and figurine ID. Uses Spring Data JPA to automatically generate the
 * required queries.
 */
@Repository
public interface FigurineEventRepository extends JpaRepository<FigurineEvent, Long> {
  /**
   * Retrieves all figurine events associated with the given figurine ID.
   *
   * @param figurineId the ID of the figurine whose events should be fetched
   * @return a list of {@link FigurineEvent} linked to the specified figurine ID
   */
  List<FigurineEvent> findAllByFigurineId(Long figurineId);

  /**
   * Retrieves a figurine event by its event ID and associated figurine ID.
   *
   * <p>Useful for ensuring the event belongs to the expected figurine.
   *
   * @param eventId the ID of the figurine event
   * @param figurineId the ID of the figurine to which the event should belong
   * @return an {@link Optional} containing the matching {@link FigurineEvent}, or empty if not
   *     found
   */
  Optional<FigurineEvent> findByIdAndFigurineId(Long eventId, Long figurineId);
}
