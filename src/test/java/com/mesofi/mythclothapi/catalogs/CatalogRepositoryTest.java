package com.mesofi.mythclothapi.catalogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.catalogs.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.catalogs.repository.IdDescRepository;
import com.mesofi.mythclothapi.common.Descriptive;

import jakarta.persistence.EntityManager;

@DataJpaTest // Bootstraps only JPA components + H2
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CatalogRepositoryTest {
  @Autowired Map<String, IdDescRepository<?, Long>> repositories;
  @Autowired EntityManager entityManager;

  /**
   * Matches catalog name to an empty **new instance** of its entity, so no casting and no
   * accidental reuse of the same object instance.
   */
  Descriptive newCatalogFor(String name) {
    return switch (name) {
      case "anniversaries" -> new Anniversary();
      case "distributions" -> new Distribution();
      case "groups" -> new Group();
      case "lineups" -> new LineUp();
      case "series" -> new Series();
      default -> throw new IllegalArgumentException("Unknown catalog name: " + name);
    };
  }

  Stream<Arguments> repositoryProvider() {
    return repositories.entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
  }

  @ParameterizedTest
  @MethodSource("repositoryProvider")
  void shouldThrowException_whenDescriptionIsNull(
      String catalogName, IdDescRepository<?, Long> repo) {
    // Arrange
    Descriptive entity = newCatalogFor(catalogName);

    // Act + Assert
    if (catalogName.equals("anniversaries")) {
      assertThatThrownBy(() -> save(repo, entity))
          .isInstanceOf(DataIntegrityViolationException.class)
          .hasMessageContaining(
              "NULL not allowed for column \"ANNIVERSARY_YEAR\""); // depends on DB dialect
    } else {
      assertThatThrownBy(() -> save(repo, entity))
          .isInstanceOf(DataIntegrityViolationException.class)
          .hasMessageContaining(
              "NULL not allowed for column \"DESCRIPTION\""); // depends on DB dialect
    }
  }

  @ParameterizedTest
  @MethodSource("repositoryProvider")
  void shouldCreateCatalog_whenValidDataProvided(
      String catalogName, IdDescRepository<?, Long> repo) {
    // Arrange
    Descriptive entity = newCatalogFor(catalogName);
    entity.setDescription("Sample " + catalogName);
    if (catalogName.equals("anniversaries")) {
      Anniversary anniversary = (Anniversary) entity;
      anniversary.setYear(50);
    }

    // Act
    Descriptive saved = save(repo, entity);

    // Assert
    assertThat(saved).as("Entity should be saved for '%s'", catalogName).isNotNull();
    assertThat(saved.getId())
        .as("Generated ID should not be null for '%s'", catalogName)
        .isNotNull();
  }

  @ParameterizedTest
  @MethodSource("repositoryProvider")
  void findById_shouldFindCatalogById_whenExists(
      String catalogName, IdDescRepository<?, Long> repo) {
    // Arrange
    Descriptive descriptiveEntity = createCatalog(catalogName);
    if (catalogName.equals("anniversaries")) {
      Anniversary anniversary = (Anniversary) descriptiveEntity;
      anniversary.setYear(50);
    }
    Descriptive saved = save(repo, descriptiveEntity);

    // Act
    Descriptive found = findById(repo, saved.getId());

    // Assert
    assertThat(found.getId()).as("Found entity must have same id").isEqualTo(saved.getId());

    assertThat(found.getDescription())
        .as("Description must match saved value")
        .isEqualTo("The Description");
  }

  @ParameterizedTest
  @MethodSource("repositoryProvider")
  void findByDescription_shouldFindCatalogByDescription_whenExists(
      String catalogName, IdDescRepository<?, Long> repo) {
    // Arrange
    Descriptive descriptiveEntity = createCatalog(catalogName, "Custom Description");
    if (catalogName.equals("anniversaries")) {
      Anniversary anniversary = (Anniversary) descriptiveEntity;
      anniversary.setYear(50);
    }
    Descriptive saved = save(repo, descriptiveEntity);

    // Act
    Descriptive found = findByDescription(repo, "Custom Description");

    // Assert
    assertThat(found.getId()).as("Found entity must have same id").isEqualTo(saved.getId());
    assertThat(found.getDescription())
        .as("Description must match saved value")
        .isEqualTo("Custom Description");
  }

  @ParameterizedTest
  @MethodSource("repositoryProvider")
  void update_shouldUpdateCatalog_whenValidChangesProvided(
      String catalogName, IdDescRepository<?, Long> repo) {
    // Arrange
    Descriptive descriptiveEntity = createCatalog(catalogName);
    if (catalogName.equals("anniversaries")) {
      Anniversary anniversary = (Anniversary) descriptiveEntity;
      anniversary.setYear(50);
    }
    Descriptive saved = save(repo, descriptiveEntity);
    Descriptive found = findById(repo, saved.getId());
    found.setDescription("Updated description");

    // Act
    Descriptive updated = save(repo, found);
    entityManager.flush();

    // Assert
    assertThat(updated.getId()).as("Found entity must have same id").isEqualTo(saved.getId());
    assertThat(found.getDescription())
        .as("Description must match saved value")
        .isEqualTo("Updated description");
  }

  @ParameterizedTest
  @MethodSource("repositoryProvider")
  void update_shouldDeleteCatalog_whenValidChangesProvided(
      String catalogName, IdDescRepository<?, Long> repo) {
    // Arrange
    Descriptive descriptiveEntity = createCatalog(catalogName);
    if (catalogName.equals("anniversaries")) {
      Anniversary anniversary = (Anniversary) descriptiveEntity;
      anniversary.setYear(50);
    }
    Descriptive saved = save(repo, descriptiveEntity);
    Descriptive found = findById(repo, saved.getId());

    // Act
    delete(repo, found);
    entityManager.flush();

    // Assert
    assertThatThrownBy(() -> findById(repo, saved.getId()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unable to find entity with id: " + saved.getId());
  }

  private Descriptive createCatalog(String catalogName) {
    return createCatalog(catalogName, "The Description");
  }

  private Descriptive createCatalog(String catalogName, String description) {
    Descriptive descriptiveEntity = newCatalogFor(catalogName);
    descriptiveEntity.setDescription(description);

    return descriptiveEntity;
  }

  /** Makes the generic type explicit and removes the raw cast noise. */
  @SuppressWarnings("unchecked")
  private <T extends Descriptive> T save(IdDescRepository<?, Long> repo, Descriptive e) {
    return ((IdDescRepository<T, Long>) repo).save((T) e);
  }

  /** Makes the generic type explicit and removes the raw cast noise. */
  @SuppressWarnings("unchecked")
  private <T extends Descriptive> T findById(IdDescRepository<?, Long> repo, long id) {
    IdDescRepository<T, Long> f = (IdDescRepository<T, Long>) repo;
    return f.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Unable to find entity with id: " + id));
  }

  /** Makes the generic type explicit and removes the raw cast noise. */
  @SuppressWarnings("unchecked")
  private <T extends Descriptive> T findByDescription(
      IdDescRepository<?, Long> repo, String description) {
    IdDescRepository<T, Long> f = (IdDescRepository<T, Long>) repo;
    return f.findByDescription(description)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Unable to find entity having description: " + description));
  }

  /** Makes the generic type explicit and removes the raw cast noise. */
  @SuppressWarnings("unchecked")
  private <T extends Descriptive> void delete(IdDescRepository<?, Long> repo, Descriptive e) {
    ((IdDescRepository<T, Long>) repo).delete((T) e);
  }
}
