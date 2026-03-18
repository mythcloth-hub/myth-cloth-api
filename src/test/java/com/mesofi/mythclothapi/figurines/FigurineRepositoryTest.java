package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.figurines.model.Figurine;

@SpringBootTest
@ActiveProfiles("test")
public class FigurineRepositoryTest {

  @Autowired FigurineRepository repository;

  @Test
  void save_shouldThrowException_whenCreationDateIsNull() {

    // Arrange
    Figurine figurine = createFigurineWithRequiredFieldsMissing();

    // Act + Assert
    assertThatThrownBy(() -> repository.saveAndFlush(figurine))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(
            "NULL not allowed for column \"CREATION_DATE\""); // depends on DB dialect
  }

  private Figurine createFigurineWithRequiredFieldsMissing() {
    Figurine figurine = new Figurine();
    figurine.setCreationDate(null);
    figurine.setUpdateDate(null);
    figurine.setNormalizedName(null);
    figurine.setLegacyName(null);
    figurine.setTamashiiUrl(null);
    return figurine;
  }
}
