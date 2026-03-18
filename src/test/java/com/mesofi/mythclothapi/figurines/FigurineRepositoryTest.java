package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

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
