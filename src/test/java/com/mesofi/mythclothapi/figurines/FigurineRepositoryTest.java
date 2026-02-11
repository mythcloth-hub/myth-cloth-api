package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.figurines.model.Figurine;

@DataJpaTest // Bootstraps only JPA components + H2
@ActiveProfiles("test")
public class FigurineRepositoryTest {
  @Autowired FigurineRepository repository;

  @Test
  void save_shouldThrowException_whenCreationDateIsNull() {
    // Arrange
    Figurine figurine = createFigurine(null, null, null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurine))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(
            "NULL not allowed for column \"CREATION_DATE\""); // depends on DB dialect
  }

  @Test
  void save_shouldThrowException_whenUpdateDateIsNull() {
    // Arrange
    Figurine figurine = createFigurine(Instant.now(), null, null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurine))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(
            "NULL not allowed for column \"UPDATE_DATE\""); // depends on DB dialect
  }

  @Test
  void save_shouldThrowException_whenNormalizedNameIsNull() {
    // Arrange
    Figurine figurine = createFigurine(Instant.now(), Instant.now(), null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurine))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(
            "NULL not allowed for column \"NORMALIZED_NAME\""); // depends on DB dialect
  }

  @Test
  void save_shouldCreateFigure_whenValidDataProvided() {
    // Arrange
    Figurine figurine = createFigurine(Instant.now(), Instant.now(), "Pegasus Seiya", null, null);

    // Act
    Figurine saved = repository.save(figurine);

    // Assert
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getNormalizedName()).isEqualTo("Pegasus Seiya");
  }

  @Test
  void save_shouldThrowException_whenLegacyNameIsDuplicated() {
    // Arrange
    Figurine figurine1 =
        createFigurine(Instant.now(), Instant.now(), "Pegasus Seiya1", "Pegasus Seiya", null);
    Figurine figurine2 =
        createFigurine(
            Instant.now(), Instant.now(), "Pegasus Seiya1", "Pegasus Seiya", null); // duplicate

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAll(List.of(figurine1, figurine2)))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(
            "PUBLIC.CONSTRAINT_INDEX_6 ON PUBLIC.FIGURINES(LEGACY_NAME NULLS FIRST)");
  }

  @Test
  void save_shouldThrowException_whenLengthIsExceeded() {
    // Arrange
    Figurine figurine =
        createFigurine(
            Instant.now(), Instant.now(), "Pegasus Seiya2", "Pegasus Seiya", "w".repeat(51));

    // Act + Assert
    assertThatThrownBy(() -> repository.save(figurine))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(
            "[Value too long for column \"TAMASHII_URL CHARACTER VARYING(50)\": \"'wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww' (51)");
  }

  @Test
  void findById_shouldFindFigurineById_whenExists() {
    // Arrange
    Figurine figurine =
        createFigurine(Instant.now(), Instant.now(), "Pegasus Seiya3", "Pegasus Seiya", null);
    Figurine saved = repository.save(figurine);

    // Act
    Figurine found = repository.findById(saved.getId()).orElse(null);

    // Assert
    assertThat(found).isNotNull();
    assertThat(found.getId()).isNotNull();
    assertThat(found.getNormalizedName()).isEqualTo("Pegasus Seiya3");
    assertThat(found.getLegacyName()).isEqualTo("Pegasus Seiya");
  }

  @Test
  void update_shouldUpdateFigurine_whenValidChangesProvided() {
    // Arrange
    Figurine figurine =
        createFigurine(Instant.now(), Instant.now(), "Pegasus Seiya", "Pegasus Seiya", null);
    Figurine saved = repository.save(figurine);

    // Act
    saved.setRemarks("Some remarks");
    Figurine updated = repository.saveAndFlush(saved);

    // Assert
    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(saved.getId());
    assertThat(updated.getRemarks()).isEqualTo("Some remarks");
  }

  @Test
  void delete_shouldDeleteFigurine_whenValidChangesProvided() {
    // Arrange
    Figurine figurine =
        createFigurine(Instant.now(), Instant.now(), "Pegasus Seiya", "Pegasus Seiya", null);
    Figurine saved = repository.save(figurine);

    // Act
    repository.delete(saved);

    // Assert
    assertThat(repository.findById(saved.getId())).isEmpty();
  }

  private Figurine createFigurine(
      Instant creationDate,
      Instant updateDate,
      String normalizedName,
      String legacyName,
      String tamashiiUrl) {
    Figurine figurine = new Figurine();
    figurine.setCreationDate(creationDate);
    figurine.setUpdateDate(updateDate);
    figurine.setNormalizedName(normalizedName);
    figurine.setLegacyName(legacyName);
    figurine.setTamashiiUrl(tamashiiUrl);
    return figurine;
  }
}
