package com.mesofi.mythclothapi.figurineevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurines.FigurineRepository;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@DataJpaTest // Bootstraps only JPA components + H2
@ActiveProfiles("test")
public class FigurineEventRepositoryTest {
  @Autowired FigurineRepository figurineRepository;
  @Autowired FigurineEventRepository repository;

  @Test
  void save_shouldThrowException_whenEventDateIsNull() {
    // Arrange
    FigurineEvent figurineEvent = createFigurineEvent(null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurineEvent))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(
            "NULL not allowed for column \"EVENT_DATE\""); // depends on DB dialect
  }

  @Test
  void save_shouldThrowException_whenDescriptionOrFigurineAreNull() {
    // Arrange
    FigurineEvent figurineEvent = createFigurineEvent(LocalDate.now(), null, null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurineEvent))
        .isInstanceOf(DataIntegrityViolationException.class)
        .matches(
            ex ->
                ex.getMessage().contains("NULL not allowed for column \"DESCRIPTION\"")
                    || ex.getMessage().contains("NULL not allowed for column \"FIGURINE_ID\""));
  }

  @Test
  void save_shouldThrowException_whenFigurineIsNull() {
    // Arrange
    FigurineEvent figurineEvent = createFigurineEvent(LocalDate.now(), "some-description", null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurineEvent))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(
            "NULL not allowed for column \"FIGURINE_ID\""); // depends on DB dialect
  }

  @Test
  void save_shouldCreateFigureEvent_whenValidDataProvided() {
    // Arrange
    Figurine figurineSaved = figurineRepository.save(createFigurine());
    FigurineEvent figurineEvent =
        createFigurineEvent(LocalDate.of(2025, 12, 6), "some-description", figurineSaved);

    // Act
    FigurineEvent saved = repository.save(figurineEvent);

    // Assert
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getEventDate()).isEqualTo(LocalDate.of(2025, 12, 6));
    assertThat(saved.getDescription()).isEqualTo("some-description");
    assertThat(saved.getFigurine().getId()).isNotNull();
  }

  @Test
  void findById_shouldFindFigurineEventById_whenExists() {
    // Arrange
    Figurine figurineSaved = figurineRepository.save(createFigurine());
    FigurineEvent figurineEvent =
        createFigurineEvent(LocalDate.of(2025, 12, 6), "some-description", figurineSaved);
    FigurineEvent saved = repository.save(figurineEvent);

    // Act
    FigurineEvent found = repository.findById(saved.getId()).orElse(null);

    // Assert
    assertThat(found).isNotNull();
    assertThat(found.getId()).isNotNull();
    assertThat(found.getEventDate()).isEqualTo(LocalDate.of(2025, 12, 6));
    assertThat(found.getDescription()).isEqualTo("some-description");
    assertThat(found.getFigurine().getId()).isNotNull();
  }

  @Test
  void findAllByFigurineId_shouldFindAllFigurineEventsByFigurineId_whenExists() {
    // Arrange
    Figurine figurineSaved = figurineRepository.save(createFigurine());
    FigurineEvent figurineEvent1 =
        createFigurineEvent(LocalDate.of(2025, 12, 6), "some-event1", figurineSaved);
    FigurineEvent figurineEvent2 =
        createFigurineEvent(LocalDate.of(2025, 12, 7), "some-event2", figurineSaved);
    repository.saveAll(List.of(figurineEvent1, figurineEvent2));

    // Act
    List<FigurineEvent> foundList = repository.findAllByFigurineId(figurineSaved.getId());

    // Assert
    assertThat(foundList)
        .isNotNull()
        .hasSize(2)
        .extracting(FigurineEvent::getDescription)
        .containsExactlyInAnyOrder("some-event1", "some-event2");

    assertThat(foundList)
        .extracting(FigurineEvent::getEventDate)
        .containsExactlyInAnyOrder(LocalDate.of(2025, 12, 6), LocalDate.of(2025, 12, 7));

    assertThat(foundList)
        .allSatisfy(
            evt -> {
              assertThat(evt.getId()).isNotNull();
              assertThat(evt.getFigurine()).isNotNull();
              assertThat(evt.getFigurine().getId()).isEqualTo(figurineSaved.getId());
            });
  }

  @Test
  void findByIdAndFigurineId_shouldFindFigurineEvent_whenExists() {
    // Arrange
    Figurine figurineSaved = figurineRepository.save(createFigurine());
    FigurineEvent figurineEvent =
        createFigurineEvent(LocalDate.of(2025, 12, 6), "some-event1", figurineSaved);
    FigurineEvent figurineEventSaved = repository.save(figurineEvent);

    // Act
    FigurineEvent found =
        repository
            .findByIdAndFigurineId(figurineSaved.getId(), figurineEventSaved.getId())
            .orElse(null);

    // Assert
    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(figurineEventSaved.getId());
    assertThat(found.getDescription()).isEqualTo("some-event1");
    assertThat(found.getEventDate()).isEqualTo(LocalDate.of(2025, 12, 6));
    assertThat(found.getFigurine()).isNotNull();
    assertThat(found.getFigurine().getId()).isEqualTo(figurineSaved.getId());
  }

  @Test
  void update_shouldUpdateFigurineEvent_whenValidChangesProvided() {
    // Arrange
    Figurine figurineSaved = figurineRepository.save(createFigurine());
    FigurineEvent figurineEvent =
        createFigurineEvent(LocalDate.of(2025, 12, 6), "some-event1", figurineSaved);
    FigurineEvent figurineEventSaved = repository.save(figurineEvent);

    // Act
    figurineEventSaved.setDescription("new event");
    FigurineEvent updated = repository.saveAndFlush(figurineEventSaved);

    // Assert
    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(figurineEventSaved.getId());
    assertThat(updated.getDescription()).isEqualTo("new event");
    assertThat(updated.getEventDate()).isEqualTo(LocalDate.of(2025, 12, 6));

    assertThat(updated.getFigurine()).isNotNull();
    assertThat(updated.getFigurine().getId()).isEqualTo(figurineSaved.getId());
  }

  @Test
  void delete_shouldDeleteFigurineEvent_whenValidChangesProvided() {
    // Arrange
    Figurine figurineSaved = figurineRepository.save(createFigurine());
    FigurineEvent figurineEvent =
        createFigurineEvent(LocalDate.of(2025, 12, 6), "some-event1", figurineSaved);
    FigurineEvent figurineEventSaved = repository.save(figurineEvent);

    // Act
    repository.delete(figurineEventSaved);

    // Assert
    assertThat(repository.findById(figurineEventSaved.getId())).isEmpty();
    assertThat(repository.findByIdAndFigurineId(figurineSaved.getId(), figurineEventSaved.getId()))
        .isEmpty();
  }

  private FigurineEvent createFigurineEvent(
      LocalDate eventDate, String description, Figurine figurine) {

    FigurineEvent figurineEvent = new FigurineEvent();
    figurineEvent.setEventDate(eventDate);
    figurineEvent.setDescription(description);
    figurineEvent.setFigurine(figurine);

    return figurineEvent;
  }

  private Figurine createFigurine() {
    Figurine figurine = new Figurine();
    figurine.setNormalizedName("Seiya");
    return figurine;
  }
}
