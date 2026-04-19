package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

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
import com.mesofi.mythclothapi.figurines.model.Figurine;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FigurineRepositoryTest {

  @Autowired FigurineRepository repository;
  @Autowired LineUpRepository lineUpRepository;
  @Autowired SeriesRepository seriesRepository;

  private LineUp savedLineUp;
  private Series savedSeries;

  @BeforeEach
  void setUp() {
    LineUp lineUp = new LineUp();
    lineUp.setDescription("Myth Cloth EX");
    savedLineUp = lineUpRepository.saveAndFlush(lineUp);

    Series series = new Series();
    series.setDescription("Saint Seiya");
    savedSeries = seriesRepository.saveAndFlush(series);
  }

  // ─── Primary Key ──────────────────────────────────────────────────────────

  @Test
  void save_shouldGeneratePrimaryKey_whenAllRequiredFieldsAreProvided() {
    // Arrange
    Figurine figurine = createValidFigurine("Pegasus Seiya EX");

    // Act
    Figurine saved = repository.saveAndFlush(figurine);

    // Assert
    assertThat(saved.getId()).isNotNull().isPositive();
  }

  // ─── NOT NULL constraints ──────────────────────────────────────────────────

  @Test
  void save_shouldThrowException_whenCreationDateIsNull() {
    // Arrange
    Figurine figurine = createValidFigurine("Pegasus Seiya EX");
    figurine.setCreationDate(null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurine))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("NULL not allowed for column \"CREATION_DATE\"");
  }

  @Test
  void save_shouldThrowException_whenUpdateDateIsNull() {
    // Arrange
    Figurine figurine = createValidFigurine("Pegasus Seiya EX");
    figurine.setUpdateDate(null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurine))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("NULL not allowed for column \"UPDATE_DATE\"");
  }

  @Test
  void save_shouldThrowException_whenNormalizedNameIsNull() {
    // Arrange
    Figurine figurine = createValidFigurine("Pegasus Seiya EX");
    figurine.setNormalizedName(null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurine))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("NULL not allowed for column \"NORMALIZED_NAME\"");
  }

  @Test
  void save_shouldThrowException_whenLineupIsNull() {
    // Arrange
    Figurine figurine = createValidFigurine("Pegasus Seiya EX");
    figurine.setLineup(null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurine))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("NULL not allowed for column \"LINEUP_ID\"");
  }

  @Test
  void save_shouldThrowException_whenSeriesIsNull() {
    // Arrange
    Figurine figurine = createValidFigurine("Pegasus Seiya EX");
    figurine.setSeries(null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurine))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("NULL not allowed for column \"SERIES_ID\"");
  }

  // ─── Unique constraint ────────────────────────────────────────────────────

  @Test
  void save_shouldThrowException_whenLegacyNameIsDuplicated() {
    // Arrange
    repository.saveAndFlush(createValidFigurine("Pegasus Seiya EX"));
    Figurine duplicate = createValidFigurine("Pegasus Seiya EX"); // same legacyName

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("Unique index or primary key violation");
  }

  @Test
  void save_shouldAllowMultipleFigurinesWithNullLegacyName() {
    // Arrange – legacyName is nullable, so two nulls must be accepted
    Figurine first = createValidFigurine(null);
    Figurine second = createValidFigurine(null);

    // Act + Assert – no exception expected
    repository.saveAndFlush(first);
    repository.saveAndFlush(second);
    assertThat(repository.count()).isGreaterThanOrEqualTo(2);
  }

  @Test
  void findByNormalizedNameContainingIgnoreCase_shouldReturnMatchingFigurines() {
    Figurine figurine1 = createValidFigurine("Pegasus Seiya EX");
    figurine1.setNormalizedName("Pegasus Seiya");
    repository.saveAndFlush(figurine1);

    Figurine figurine2 = createValidFigurine("Dragon Shiryu EX");
    figurine2.setNormalizedName("Dragon Shiryu");
    repository.saveAndFlush(figurine2);

    var page =
        repository.findByNormalizedNameContainingIgnoreCase(
            "seiya", org.springframework.data.domain.PageRequest.of(0, 10));
    assertThat(page.getContent()).extracting(Figurine::getNormalizedName).contains("Pegasus Seiya");
    assertThat(page.getContent()).doesNotContain(figurine2);
  }

  @Test
  void findByNormalizedNameContainingIgnoreCase_shouldReturnEmpty_whenNoMatch() {
    var page =
        repository.findByNormalizedNameContainingIgnoreCase(
            "xyz", org.springframework.data.domain.PageRequest.of(0, 10));
    assertThat(page.getContent()).isEmpty();
  }

  // ─── Helper ───────────────────────────────────────────────────────────────

  private Figurine createValidFigurine(String legacyName) {
    Figurine figurine = new Figurine();
    figurine.setLegacyName(legacyName);
    figurine.setNormalizedName("pegasus-seiya-ex");
    figurine.setLineup(savedLineUp);
    figurine.setSeries(savedSeries);
    figurine.setCreationDate(Instant.now());
    figurine.setUpdateDate(Instant.now());
    return figurine;
  }
}
