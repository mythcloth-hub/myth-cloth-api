package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.distributors.DistributorRepository;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FigurineRepositoryTest {

  @Autowired FigurineRepository repository;
  @Autowired LineUpRepository lineUpRepository;
  @Autowired SeriesRepository seriesRepository;
  @Autowired DistributorRepository distributorRepository;

  @PersistenceContext EntityManager em;

  private LineUp savedLineUp;
  private Series savedSeries;
  private Distributor savedDistributor;

  @BeforeEach
  void setUp() {
    LineUp lineUp = new LineUp();
    lineUp.setDescription("Myth Cloth EX");
    savedLineUp = lineUpRepository.saveAndFlush(lineUp);

    Series series = new Series();
    series.setDescription("Saint Seiya");
    savedSeries = seriesRepository.saveAndFlush(series);

    Distributor distributor = new Distributor();
    distributor.setName(DistributorName.BANDAI);
    distributor.setCountry(CountryCode.JP);
    savedDistributor = distributorRepository.saveAndFlush(distributor);
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
  void search_shouldReturnMatchingFigurines() {
    Figurine figurine1 = createValidFigurine("Pegasus Seiya EX");
    figurine1.setNormalizedName("Pegasus Seiya");
    repository.saveAndFlush(figurine1);

    Figurine figurine2 = createValidFigurine("Dragon Shiryu EX");
    figurine2.setNormalizedName("Dragon Shiryu");
    repository.saveAndFlush(figurine2);

    var filter =
        new FigurineFilter(
            "seiya", null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);
    var page = repository.search(filter, org.springframework.data.domain.PageRequest.of(0, 10));
    assertThat(page.getContent()).extracting(Figurine::getNormalizedName).contains("Pegasus Seiya");
    assertThat(page.getContent())
        .extracting(Figurine::getNormalizedName)
        .doesNotContain("Dragon Shiryu");
  }

  @Test
  void search_shouldReturnEmpty_whenNoMatch() {
    var filter =
        new FigurineFilter(
            "xyz", null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);
    var page = repository.search(filter, org.springframework.data.domain.PageRequest.of(0, 10));
    assertThat(page.getContent()).isEmpty();
  }

  @Test
  void search_shouldFilterByAllBooleans() {
    Figurine figurine = createValidFigurine("BooleanTest");
    figurine.setMetalBody(true);
    figurine.setOce(true);
    figurine.setRevival(true);
    figurine.setPlainCloth(true);
    figurine.setBroken(true);
    figurine.setGolden(true);
    figurine.setGold(true);
    figurine.setManga(true);
    figurine.setSet(true);
    figurine.setArticulable(true);
    repository.saveAndFlush(figurine);

    FigurineFilter filter =
        new FigurineFilter(
            null, // name
            null, // lineUpId
            null, // seriesId
            null, // groupId
            true, // metalBody
            true, // oce
            true, // revival
            true, // plainCloth
            true, // broken
            true, // golden
            true, // gold
            true, // manga
            true, // set
            true, // articulable
            null // releaseStatus
            );
    var page = repository.search(filter, PageRequest.of(0, 10));
    assertThat(page.getContent())
        .extracting(Figurine::getNormalizedName)
        .contains("pegasus-seiya-ex");
  }

  @Test
  void search_shouldFilterByLineUpId() {
    Figurine figurine = createValidFigurine("LineUpTest");
    repository.saveAndFlush(figurine);
    FigurineFilter filter =
        new FigurineFilter(
            null,
            savedLineUp.getId(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    var page = repository.search(filter, PageRequest.of(0, 10));
    assertThat(page.getContent()).isNotEmpty();
  }

  @Test
  void search_shouldFilterBySeriesId() {
    Figurine figurine = createValidFigurine("SeriesTest");
    repository.saveAndFlush(figurine);
    FigurineFilter filter =
        new FigurineFilter(
            null,
            null,
            savedSeries.getId(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    var page = repository.search(filter, PageRequest.of(0, 10));
    assertThat(page.getContent()).isNotEmpty();
  }

  @Test
  void search_shouldReturnCorrectReleaseStatus() {
    // RUMORED: no announcement_date, no release_date
    Figurine rumored = createValidFigurine("Rumored");
    repository.saveAndFlush(rumored);
    // PROTOTYPE: announcement_date set, release_date null
    Figurine prototype = createValidFigurine("Prototype");
    repository.saveAndFlush(prototype);
    em.createNativeQuery(
            "UPDATE figurine_distributor SET announcement_date = ? WHERE figurine_id = ?")
        .setParameter(1, LocalDate.now())
        .setParameter(2, prototype.getId())
        .executeUpdate();
    // ANNOUNCED: release_date in future
    Figurine announced = createValidFigurine("Announced");
    repository.saveAndFlush(announced);
    em.createNativeQuery("UPDATE figurine_distributor SET release_date = ? WHERE figurine_id = ?")
        .setParameter(1, LocalDate.now().plusDays(10))
        .setParameter(2, announced.getId())
        .executeUpdate();
    // RELEASED: release_date in past
    Figurine released = createValidFigurine("Released");
    repository.saveAndFlush(released);
    em.createNativeQuery("UPDATE figurine_distributor SET release_date = ? WHERE figurine_id = ?")
        .setParameter(1, LocalDate.now().minusDays(10))
        .setParameter(2, released.getId())
        .executeUpdate();
    // Test each status
    for (String status : List.of("RUMORED", "PROTOTYPE", "ANNOUNCED", "RELEASED")) {
      FigurineFilter filter =
          new FigurineFilter(
              null, null, null, null, null, null, null, null, null, null, null, null, null, null,
              status);
      var page = repository.search(filter, PageRequest.of(0, 10));
      assertThat(page.getContent()).isNotEmpty();
    }
  }

  @Test
  void search_shouldSupportPagination() {
    for (int i = 0; i < 15; i++) {
      Figurine figurine = createValidFigurine("Paginate" + i);
      figurine.setNormalizedName("Paginate" + i);
      repository.saveAndFlush(figurine);
    }
    var filter =
        new FigurineFilter(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);
    var page1 = repository.search(filter, PageRequest.of(0, 5));
    var page2 = repository.search(filter, PageRequest.of(1, 5));
    assertThat(page1.getContent()).hasSize(5);
    assertThat(page2.getContent()).hasSize(5);
  }

  @Test
  void search_shouldReturnAll_whenAllFiltersNull() {
    Figurine figurine = createValidFigurine("AllNull");
    repository.saveAndFlush(figurine);
    var filter =
        new FigurineFilter(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);
    var page = repository.search(filter, PageRequest.of(0, 10));
    assertThat(page.getContent()).isNotEmpty();
  }

  @Test
  void search_shouldReturnEmpty_whenDatabaseIsEmpty() {
    repository.deleteAll();
    var filter =
        new FigurineFilter(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);
    var page = repository.search(filter, PageRequest.of(0, 10));
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

    // Add a valid distributor (required for release status logic)
    FigurineDistributor distributor = new FigurineDistributor();
    distributor.setFigurine(figurine);
    distributor.setDistributor(savedDistributor);
    distributor.setCurrency(CurrencyCode.JPY);
    distributor.setReleaseDateConfirmed(false);
    figurine.getDistributors().add(distributor);

    return figurine;
  }
}
