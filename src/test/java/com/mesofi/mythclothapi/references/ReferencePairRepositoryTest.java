package com.mesofi.mythclothapi.references;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

import com.mesofi.mythclothapi.entity.DescriptiveEntity;
import com.mesofi.mythclothapi.references.entity.DistributionEntity;
import com.mesofi.mythclothapi.references.entity.GroupEntity;
import com.mesofi.mythclothapi.references.entity.LineUpEntity;
import com.mesofi.mythclothapi.references.entity.SeriesEntity;
import com.mesofi.mythclothapi.references.repository.IdDescPairRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest // Bootstraps only JPA components + H2
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReferencePairRepositoryTest {
  @Autowired Map<String, IdDescPairRepository<?, Long>> repositories;
  @Autowired EntityManager entityManager;

  /**
   * Matches reference name to an empty **new instance** of its entity, so no casting and no
   * accidental reuse of the same object instance.
   */
  DescriptiveEntity newEntityFor(String name) {
    return switch (name) {
      case "distributions" -> new DistributionEntity();
      case "groups" -> new GroupEntity();
      case "lineups" -> new LineUpEntity();
      case "series" -> new SeriesEntity();
      default -> throw new IllegalArgumentException("Unknown reference name: " + name);
    };
  }

  Stream<Arguments> repositoryProvider() {
    return repositories.entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
  }

  @ParameterizedTest
  @MethodSource("repositoryProvider")
  void shouldThrowException_whenDescriptionIsNull(
      String referenceName, IdDescPairRepository<?, Long> repo) {
    // Arrange
    DescriptiveEntity entity = newEntityFor(referenceName);

    // Act + Assert
    assertThatThrownBy(() -> save(repo, entity))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(
            "NULL not allowed for column \"DESCRIPTION\""); // depends on DB dialect
  }

  @ParameterizedTest
  @MethodSource("repositoryProvider")
  void shouldCreateReference_whenValidDataProvided(
      String referenceName, IdDescPairRepository<?, Long> repo) {
    // Arrange
    DescriptiveEntity entity = newEntityFor(referenceName);
    entity.setDescription("Sample " + referenceName);

    // Act
    DescriptiveEntity saved = save(repo, entity);

    // Assert
    assertThat(saved).as("Entity should be saved for '%s'", referenceName).isNotNull();
    assertThat(saved.getId())
        .as("Generated ID should not be null for '%s'", referenceName)
        .isNotNull();
  }

  @ParameterizedTest
  @MethodSource("repositoryProvider")
  void findById_shouldFindReferenceById_whenExists(
      String referenceName, IdDescPairRepository<?, Long> repo) {
    // Arrange
    DescriptiveEntity descriptiveEntity = createReference(referenceName);
    DescriptiveEntity saved = save(repo, descriptiveEntity);

    // Act
    DescriptiveEntity found = findById(repo, saved.getId());

    // Assert
    assertThat(found.getId()).as("Found entity must have same id").isEqualTo(saved.getId());

    assertThat(found.getDescription())
        .as("Description must match saved value")
        .isEqualTo("The Description");
  }

  @ParameterizedTest
  @MethodSource("repositoryProvider")
  void update_shouldUpdateReference_whenValidChangesProvided(
      String referenceName, IdDescPairRepository<?, Long> repo) {
    // Arrange
    DescriptiveEntity descriptiveEntity = createReference(referenceName);
    DescriptiveEntity saved = save(repo, descriptiveEntity);
    DescriptiveEntity found = findById(repo, saved.getId());
    found.setDescription("Updated description");

    // Act
    DescriptiveEntity updated = save(repo, found);
    entityManager.flush();

    // Assert
    assertThat(updated.getId()).as("Found entity must have same id").isEqualTo(saved.getId());
    assertThat(found.getDescription())
        .as("Description must match saved value")
        .isEqualTo("Updated description");
  }

  @ParameterizedTest
  @MethodSource("repositoryProvider")
  void update_shouldDeleteReference_whenValidChangesProvided(
      String referenceName, IdDescPairRepository<?, Long> repo) {
    // Arrange
    DescriptiveEntity descriptiveEntity = createReference(referenceName);
    DescriptiveEntity saved = save(repo, descriptiveEntity);
    DescriptiveEntity found = findById(repo, saved.getId());

    // Act
    delete(repo, found);
    entityManager.flush();

    // Assert
    assertThatThrownBy(() -> findById(repo, saved.getId()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unable to find entity with id: " + saved.getId());
  }

  private DescriptiveEntity createReference(String referenceName) {
    DescriptiveEntity descriptiveEntity = newEntityFor(referenceName);
    descriptiveEntity.setDescription("The Description");

    return descriptiveEntity;
  }

  /** Makes the generic type explicit and removes the raw cast noise. */
  @SuppressWarnings("unchecked")
  private <T extends DescriptiveEntity> T save(
      IdDescPairRepository<?, Long> repo, DescriptiveEntity e) {
    return ((IdDescPairRepository<T, Long>) repo).save((T) e);
  }

  /** Makes the generic type explicit and removes the raw cast noise. */
  @SuppressWarnings("unchecked")
  private <T extends DescriptiveEntity> T findById(IdDescPairRepository<?, Long> repo, long id) {
    IdDescPairRepository<T, Long> f = (IdDescPairRepository<T, Long>) repo;
    return f.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Unable to find entity with id: " + id));
  }

  /** Makes the generic type explicit and removes the raw cast noise. */
  @SuppressWarnings("unchecked")
  private <T extends DescriptiveEntity> void delete(
      IdDescPairRepository<?, Long> repo, DescriptiveEntity e) {
    ((IdDescPairRepository<T, Long>) repo).delete((T) e);
  }
}
