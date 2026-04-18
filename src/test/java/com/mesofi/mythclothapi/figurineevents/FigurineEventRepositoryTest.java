package com.mesofi.mythclothapi.figurineevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurines.FigurineRepository;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FigurineEventRepositoryTest {
  @Autowired LineUpRepository lineUpRepository;
  @Autowired SeriesRepository seriesRepository;
  @Autowired FigurineRepository figurineRepository;
  @Autowired FigurineEventRepository repository;

  private LineUp savedLineUp;
  private Series savedSeries;

  @BeforeEach
  void setUp() {
    LineUp lineUp = new LineUp();
    lineUp.setDescription("lineUp");
    savedLineUp = lineUpRepository.saveAndFlush(lineUp);

    Series series = new Series();
    series.setDescription("series");
    savedSeries = seriesRepository.saveAndFlush(series);
  }

  @Test
  void save_shouldThrowException_whenEventDateIsNull() {
    // Arrange
    FigurineEvent figurineEvent = createFigurineEvent(null, null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurineEvent))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(
            "NULL not allowed for column \"EVENT_DATE\""); // depends on DB dialect
  }

  @Test
  void save_shouldThrowException_whenDescriptionOrFigurineAreNull() {
    // Arrange
    FigurineEvent figurineEvent = createFigurineEvent(LocalDate.now(), null, null, CountryCode.CN);

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
    FigurineEvent figurineEvent =
        createFigurineEvent(LocalDate.now(), "some-description", null, null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurineEvent))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void save_shouldCreateFigureEvent_whenValidDataProvided() {
    // Arrange
    Figurine figurineSaved = figurineRepository.save(createFigurine());
    FigurineEvent figurineEvent =
        createFigurineEvent(
            LocalDate.of(2025, 12, 6), "some-description", figurineSaved, CountryCode.US);

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
        createFigurineEvent(
            LocalDate.of(2025, 12, 6), "some-description", figurineSaved, CountryCode.US);
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
        createFigurineEvent(
            LocalDate.of(2025, 12, 6), "some-event1", figurineSaved, CountryCode.CN);
    FigurineEvent figurineEvent2 =
        createFigurineEvent(
            LocalDate.of(2025, 12, 7), "some-event2", figurineSaved, CountryCode.CN);
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
        createFigurineEvent(
            LocalDate.of(2025, 12, 6), "some-event1", figurineSaved, CountryCode.US);
    FigurineEvent figurineEventSaved = repository.save(figurineEvent);

    // Act
    FigurineEvent found =
        repository
            .findByIdAndFigurineId(figurineEventSaved.getId(), figurineSaved.getId())
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
        createFigurineEvent(
            LocalDate.of(2025, 12, 6), "some-event1", figurineSaved, CountryCode.US);
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
        createFigurineEvent(
            LocalDate.of(2025, 12, 6), "some-event1", figurineSaved, CountryCode.US);
    FigurineEvent figurineEventSaved = repository.save(figurineEvent);

    // Act
    repository.delete(figurineEventSaved);

    // Assert
    assertThat(repository.findById(figurineEventSaved.getId())).isEmpty();
    assertThat(repository.findByIdAndFigurineId(figurineEventSaved.getId(), figurineSaved.getId()))
        .isEmpty();
  }

  private FigurineEvent createFigurineEvent(
      LocalDate eventDate, String description, Figurine figurine, CountryCode region) {

    FigurineEvent figurineEvent = new FigurineEvent();
    figurineEvent.setEventDate(eventDate);
    figurineEvent.setDescription(description);
    figurineEvent.setFigurine(figurine);
    figurineEvent.setRegion(region);
    figurineEvent.setType(FigurineEventType.ANNOUNCEMENT);

    return figurineEvent;
  }

  private Figurine createFigurine() {
    Figurine figurine = new Figurine();
    figurine.setNormalizedName("Seiya");
    figurine.setLineup(savedLineUp);
    figurine.setSeries(savedSeries);
    figurine.setCreationDate(Instant.now());
    figurine.setUpdateDate(Instant.now());
    return figurine;
  }
}
