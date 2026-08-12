package com.mesofi.mythclothapi.figurineimports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class FigurineImportRepositoryTest {

    @Autowired
    private FigurineImportRepository figurineImportRepository;

    @Test
    void shouldThrowException_whenCompletedAtIsNull() {
        // Arrange
        FigurineImport figurineImport = new FigurineImport();

        // Act + Assert
        assertThatThrownBy(() -> figurineImportRepository.saveAndFlush(figurineImport))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldPersistFigurineImport_whenRequiredFieldsArePresent() {
        // Arrange
        FigurineImport figurineImport = new FigurineImport();
        figurineImport.setTotalImported(100);
        figurineImport.setCompletedAt(Instant.now());
        figurineImport.setCreationDate(Instant.now());
        figurineImport.setUpdateDate(Instant.now());

        // Act
        FigurineImport saved = figurineImportRepository.saveAndFlush(figurineImport);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTotalImported()).isEqualTo(100);
        assertThat(saved.getCompletedAt()).isNotNull();
        assertThat(saved.getErrorMessage()).isNull();
        assertThat(saved.getCreationDate()).isNotNull();
        assertThat(saved.getUpdateDate()).isNotNull();
    }

    @Test
    void shouldPersistFigurineImport_whenErrorMessageIsPresent() {
        // Arrange
        FigurineImport figurineImport = new FigurineImport();
        figurineImport.setTotalImported(50);
        figurineImport.setErrorMessage("Unable to process some figurines.");
        figurineImport.setCompletedAt(Instant.now());
        figurineImport.setCreationDate(Instant.now());
        figurineImport.setUpdateDate(Instant.now());

        // Act
        FigurineImport saved = figurineImportRepository.saveAndFlush(figurineImport);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getErrorMessage()).isEqualTo("Unable to process some figurines.");
        assertThat(saved.getCreationDate()).isNotNull();
        assertThat(saved.getUpdateDate()).isNotNull();
    }
}