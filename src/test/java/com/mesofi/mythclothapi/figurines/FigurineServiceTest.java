package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.MX;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.anniversaries.Anniversary;
import com.mesofi.mythclothapi.anniversaries.AnniversaryRepository;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.catalogs.repository.DistributionRepository;
import com.mesofi.mythclothapi.catalogs.repository.GroupRepository;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.config.MapperTestConfig;
import com.mesofi.mythclothapi.config.MethodValidationTestConfig;
import com.mesofi.mythclothapi.config.TestCsvConfig;
import com.mesofi.mythclothapi.distributors.DistributorRepository;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurines.dto.DistributorReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineDistributorResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.imports.FigurineCsvSource;
import com.mesofi.mythclothapi.figurines.mapper.CatalogContext;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;

@SpringBootTest(
    classes = {
      FigurineService.class,
      MapperTestConfig.class,
      MethodValidationTestConfig.class,
      TestCsvConfig.class
    })
public class FigurineServiceTest {

  @Autowired private FigurineService figurineService;
  @Autowired private FigurineMapper mapper;

  @MockitoBean private DistributorRepository distributorRepository;
  @MockitoBean private DistributionRepository distributionRepository;
  @MockitoBean private LineUpRepository lineUpRepository;
  @MockitoBean private SeriesRepository seriesRepository;
  @MockitoBean private GroupRepository groupRepository;
  @MockitoBean private AnniversaryRepository anniversaryRepository;
  @MockitoBean private FigurineRepository figurineRepository;
  @MockitoBean private CurrencyRegionResolver currencyRegionResolver;
  @MockitoBean private FigurineCsvSource figurineCsvSource;

  @Test
  void importFromPublicDrive_shouldThrowIllegalStateException_whenCsvSourceOpenFails()
      throws IOException {
    // Arrange
    mockCatalogRepositories();

    IOException rootCause = new IOException("boom");
    when(figurineCsvSource.openReader()).thenThrow(rootCause);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.importFromPublicDrive())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to read CSV from Google Drive")
        .hasCause(rootCause);

    verifyCatalogRepositoryInteractions();
    verify(figurineRepository, never()).saveAllAndFlush(any());
  }

  @Test
  void importFromPublicDrive_shouldSaveAllFigurines_whenAllFigurinesAreDifferent()
      throws IOException {
    // Arrange
    List<Figurine> figurines = new ArrayList<>();
    for (int i = 0; i < 12; i++) {
      figurines.add(new Figurine());
    }

    String filename = "MythCloth Catalog - CatalogMyth.csv";
    mockCatalogRepositories();
    when(figurineCsvSource.openReader()).thenReturn(loadImportCsvFixture(filename));
    when(figurineRepository.saveAllAndFlush(any())).thenReturn(figurines);

    // Act
    figurineService.importFromPublicDrive();

    // Verify
    verifyCatalogRepositoryInteractions();
    verify(figurineCsvSource).openReader();

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Iterable<Figurine>> captor = ArgumentCaptor.forClass(Iterable.class);
    verify(figurineRepository).saveAllAndFlush(captor.capture());
    captor
        .getValue()
        .forEach(
            figurine -> {
              assertThat(figurine).isNotNull();
              assertThat(figurine.getCreationDate()).isNotNull();
              assertThat(figurine.getUpdateDate()).isNotNull();
            });
  }

  @Test
  void importFromPublicDrive_shouldSaveAllFigurines_whenSomeFigurinesAreDuplicates()
      throws IOException {
    // Arrange
    List<Figurine> figurines = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      figurines.add(new Figurine());
    }

    String filename = "MythCloth Catalog - CatalogMythDuplicates.csv";
    mockCatalogRepositories();
    when(figurineCsvSource.openReader()).thenReturn(loadImportCsvFixture(filename));
    when(figurineRepository.saveAllAndFlush(any())).thenReturn(figurines);

    // Act
    figurineService.importFromPublicDrive();

    // Verify
    verifyCatalogRepositoryInteractions();
    verify(figurineCsvSource).openReader();

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Iterable<Figurine>> captor = ArgumentCaptor.forClass(Iterable.class);
    verify(figurineRepository).saveAllAndFlush(captor.capture());
    Iterable<Figurine> iterable = captor.getValue();
    int i = 0;
    for (Figurine figurine : iterable) {
      if (i == 0) {
        assertThat(figurine.getLegacyName()).isEqualTo("Poseidon EX OCE");
        assertThat(figurine.getDistributors().isEmpty()).isFalse();
        figurine
            .getDistributors()
            .forEach(distributor -> assertThat(distributor.getFigurine()).isNotNull());
        figurine.getEvents().forEach(event -> assertThat(event.getFigurine()).isNotNull());
      }
      if (i == 1) {
        assertThat(figurine.getLegacyName()).isEqualTo("Odin Seiya EX");
        assertThat(figurine.getDistributors().isEmpty()).isFalse();
        figurine
            .getDistributors()
            .forEach(distributor -> assertThat(distributor.getFigurine()).isNotNull());
        figurine.getEvents().forEach(event -> assertThat(event.getFigurine()).isNotNull());
      }
      if (i == 2) {
        assertThat(figurine.getLegacyName()).isEqualTo("Poseidon EX OCE");
        assertThat(figurine.getDistributors().isEmpty()).isFalse();
      }
      i++;
    }
  }

  @Test
  @SuppressWarnings("DataFlowIssue")
  void createFigurine_shouldThrowConstraintViolationException_whenRequestIsNull() {
    // Act
    assertThatThrownBy(() -> figurineService.createFigurine(null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurine.request: must not be null");
  }

  @Test
  void createFigurine_shouldThrowConstraintViolationException_whenRequestHasInvalidFields() {
    // Arrange
    FigurineReq figurineReq = createFigurineReq(null, null, null, null, null);

    // Act
    assertThatThrownBy(() -> figurineService.createFigurine(figurineReq))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurine.request.name: must not be blank")
        .hasMessageContaining("createFigurine.request.lineUpId: must not be null")
        .hasMessageContaining("createFigurine.request.seriesId: must not be null");
  }

  @Test
  void createFigurine_shouldThrowConstraintViolationException_whenRequestHasFieldValueViolations() {
    // Arrange
    FigurineReq figurineReq =
        createFigurineReq(
            "Pegasus Seiya".repeat(20),
            "https://tamashiiweb.com/item/15834/".repeat(5),
            -1L,
            -1L,
            null);

    // Act
    assertThatThrownBy(() -> figurineService.createFigurine(figurineReq))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurine.request.name: Name must not exceed 100 characters")
        .hasMessageContaining(
            "createFigurine.request.tamashiiUrl: Tamashii URL must not exceed 50 characters")
        .hasMessageContaining("createFigurine.request.lineUpId: must be greater than 0")
        .hasMessageContaining("createFigurine.request.seriesId: must be greater than 0");
  }

  @Test
  // @SuppressWarnings("DataFlowIssue")
  void createFigurine_shouldThrowConstraintViolationException_whenDistributorFieldsAreInvalid() {
    // Arrange
    List<DistributorReq> distributors = new ArrayList<>();
    distributors.add(new DistributorReq(-1L, null, -10.0, null, null, null, null));

    FigurineReq figurineReq =
        createFigurineReq(
            "Pegasus Seiya", "https://tamashiiweb.com/item/15834/", 1L, 1L, distributors);

    // Act
    assertThatThrownBy(() -> figurineService.createFigurine(figurineReq))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining(
            "createFigurine.request.distributors[0].price: must be greater than 0")
        .hasMessageContaining("createFigurine.request.distributors[0].currency: must not be null")
        .hasMessageContaining(
            "createFigurine.request.distributors[0].supplierId: must be greater than 0");
  }

  @Test
  void createFigurine_shouldThrowCatalogNotFoundException_whenLineUpDoesNotExist() {
    // Arrange
    List<DistributorReq> distributors = new ArrayList<>();
    distributors.add(new DistributorReq(1L, CurrencyCode.JPY, 10.0, null, null, null, null));

    FigurineReq figurineReq =
        createFigurineReq(
            "Pegasus Seiya", "https://tamashiiweb.com/item/15834/", 1L, 1L, distributors);

    // Act
    assertThatThrownBy(() -> figurineService.createFigurine(figurineReq))
        .isInstanceOf(CatalogNotFoundException.class)
        .hasMessageContaining("Catalog not found: LineUp not found for id=1");
  }

  @ParameterizedTest
  @NullAndEmptySource
  void createFigurine_shouldReturnFigurineResp_whenRequestIsBasic(
      List<DistributorReq> distributorReqList) {
    // Arrange
    CatalogContext catalogContext = mockCatalogRepositories();

    FigurineReq figurineReq =
        createFigurineReq(
            "Pegasus Seiya", "https://tamashiiweb.com/item/15834/", 1L, 1L, distributorReqList);

    Figurine figurineSaved = mapper.toFigurine(figurineReq, catalogContext);
    figurineSaved.setId(99L);

    when(figurineRepository.save(any())).thenReturn(figurineSaved);

    // Act
    FigurineResp figurineResp = figurineService.createFigurine(figurineReq);

    // Assert
    assertThat(figurineResp)
        .isNotNull()
        .extracting(
            FigurineResp::id,
            FigurineResp::name,
            FigurineResp::displayableName,
            FigurineResp::distributors,
            FigurineResp::tamashiiUrl,
            FigurineResp::releaseStatus,
            FigurineResp::distribution,
            FigurineResp::lineUp,
            FigurineResp::series,
            FigurineResp::group,
            FigurineResp::anniversary,
            FigurineResp::isMetalBody,
            FigurineResp::isOriginalColorEdition,
            FigurineResp::isRevival,
            FigurineResp::isPlainCloth,
            FigurineResp::isBattleDamaged,
            FigurineResp::isGoldenArmor,
            FigurineResp::isGold24kEdition,
            FigurineResp::isMangaVersion,
            FigurineResp::isMultiPack,
            FigurineResp::isArticulable,
            FigurineResp::notes,
            FigurineResp::officialImageUrls,
            FigurineResp::unofficialImageUrls,
            FigurineResp::events,
            FigurineResp::createdAt,
            FigurineResp::updatedAt)
        .containsExactly(
            99L,
            "Pegasus Seiya",
            "FIXME",
            distributorReqList,
            "https://tamashiiweb.com/item/15834/",
            ReleaseStatus.RUMORED,
            null,
            new CatalogResp(1, "Myth Cloth EX"),
            new CatalogResp(1, "Saint Seiya"),
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
            null,
            null,
            null,
            List.of(),
            null,
            null);

    // Verify
    verifyCatalogRepositoryInteractions();
    verify(figurineRepository).save(any());
  }

  @Test
  void createFigurine_shouldReturnFigurineResp_whenRequestIsValid() {
    // Arrange
    CatalogContext catalogContext = mockCatalogRepositories();
    List<DistributorReq> distributors = new ArrayList<>();
    distributors.add(new DistributorReq(1L, CurrencyCode.JPY, 16000.0d, null, null, null, null));

    FigurineReq figurineReq =
        createFigurineReq(
            "Pegasus Seiya", "https://tamashiiweb.com/item/15834/", 1L, 1L, distributors);

    Figurine figurineSaved = mapper.toFigurine(figurineReq, catalogContext);
    figurineSaved.setId(99L);

    when(figurineRepository.save(any())).thenReturn(figurineSaved);

    // Act
    FigurineResp figurineResp = figurineService.createFigurine(figurineReq);

    // Assert
    assertThat(figurineResp)
        .isNotNull()
        .extracting(
            FigurineResp::id,
            FigurineResp::name,
            FigurineResp::displayableName,
            FigurineResp::distributors,
            FigurineResp::tamashiiUrl,
            FigurineResp::releaseStatus,
            FigurineResp::distribution,
            FigurineResp::lineUp,
            FigurineResp::series,
            FigurineResp::group,
            FigurineResp::anniversary,
            FigurineResp::isMetalBody,
            FigurineResp::isOriginalColorEdition,
            FigurineResp::isRevival,
            FigurineResp::isPlainCloth,
            FigurineResp::isBattleDamaged,
            FigurineResp::isGoldenArmor,
            FigurineResp::isGold24kEdition,
            FigurineResp::isMangaVersion,
            FigurineResp::isMultiPack,
            FigurineResp::isArticulable,
            FigurineResp::notes,
            FigurineResp::officialImageUrls,
            FigurineResp::unofficialImageUrls,
            FigurineResp::events,
            FigurineResp::createdAt,
            FigurineResp::updatedAt)
        .containsExactly(
            99L,
            "Pegasus Seiya",
            "FIXME",
            List.of(
                new FigurineDistributorResp(
                    new DistributorResp(1, "BANDAI", "Tamashii Nations", "JP", null),
                    JPY,
                    16000.0d,
                    16000.0d,
                    null,
                    null,
                    null,
                    false)),
            "https://tamashiiweb.com/item/15834/",
            ReleaseStatus.RUMORED,
            null,
            new CatalogResp(1, "Myth Cloth EX"),
            new CatalogResp(1, "Saint Seiya"),
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
            null,
            null,
            null,
            List.of(),
            null,
            null);

    // Verify
    verifyCatalogRepositoryInteractions();
    verify(figurineRepository).save(any());
  }

  @Test
  void createFigurine_shouldReturnFigurineResp_whenRequestIsValidWithEvents() {
    // Arrange
    CatalogContext catalogContext = mockCatalogRepositories();
    List<DistributorReq> distributors = new ArrayList<>();
    distributors.add(
        new DistributorReq(
            1L,
            CurrencyCode.JPY,
            16000.0d,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 2, 2),
            LocalDate.of(2026, 3, 3),
            true));

    FigurineReq figurineReq =
        createFigurineReq(
            "Pegasus Seiya", "https://tamashiiweb.com/item/15834/", 1L, 1L, distributors);

    Figurine figurineSaved = mapper.toFigurine(figurineReq, catalogContext);
    figurineSaved.setId(99L);

    when(currencyRegionResolver.resolveCountry(JPY)).thenReturn(JP);
    when(figurineRepository.save(any())).thenReturn(figurineSaved);

    // Act
    FigurineResp figurineResp = figurineService.createFigurine(figurineReq);

    // Assert
    ArgumentCaptor<Figurine> captor = ArgumentCaptor.forClass(Figurine.class);

    verify(figurineRepository).save(captor.capture());
    Figurine persisted = captor.getValue();
    assertThat(persisted.getId()).isNull();
    assertThat(persisted.getEvents().size()).isEqualTo(3);
    persisted
        .getEvents()
        .forEach(
            event -> {
              assertThat(event.getFigurine()).isNotNull();
              assertThat(event.getEventDate())
                  .isIn(
                      LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 2), LocalDate.of(2026, 3, 3));
              assertThat(event.isEventDateConfirmed()).isTrue();
              assertThat(event.getRegion()).isEqualTo(JP);
              assertThat(event.getType())
                  .isIn(
                      FigurineEventType.ANNOUNCEMENT,
                      FigurineEventType.PREORDER_OPEN,
                      FigurineEventType.RELEASE);
              assertThat(event.getDescription())
                  .isIn(
                      "First announced as a possible future release.",
                      "Pre-orders are officially open.",
                      "The global release date has been officially announced.");
            });

    assertThat(figurineResp)
        .isNotNull()
        .extracting(
            FigurineResp::id,
            FigurineResp::name,
            FigurineResp::displayableName,
            FigurineResp::distributors,
            FigurineResp::tamashiiUrl,
            FigurineResp::releaseStatus,
            FigurineResp::distribution,
            FigurineResp::lineUp,
            FigurineResp::series,
            FigurineResp::group,
            FigurineResp::anniversary,
            FigurineResp::isMetalBody,
            FigurineResp::isOriginalColorEdition,
            FigurineResp::isRevival,
            FigurineResp::isPlainCloth,
            FigurineResp::isBattleDamaged,
            FigurineResp::isGoldenArmor,
            FigurineResp::isGold24kEdition,
            FigurineResp::isMangaVersion,
            FigurineResp::isMultiPack,
            FigurineResp::isArticulable,
            FigurineResp::notes,
            FigurineResp::officialImageUrls,
            FigurineResp::unofficialImageUrls,
            FigurineResp::events,
            FigurineResp::createdAt,
            FigurineResp::updatedAt)
        .containsExactly(
            99L,
            "Pegasus Seiya",
            "FIXME",
            List.of(
                new FigurineDistributorResp(
                    new DistributorResp(1, "BANDAI", "Tamashii Nations", "JP", null),
                    JPY,
                    16000.0d,
                    17600.0d,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 2, 2),
                    LocalDate.of(2026, 3, 3),
                    true)),
            "https://tamashiiweb.com/item/15834/",
            ReleaseStatus.RELEASED,
            null,
            new CatalogResp(1, "Myth Cloth EX"),
            new CatalogResp(1, "Saint Seiya"),
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
            null,
            null,
            null,
            List.of(),
            null,
            null);

    // Verify
    verifyCatalogRepositoryInteractions();
  }

  private FigurineReq createFigurineReq(
      String name,
      String tamashiiUrl,
      Long lineUpId,
      Long seriesId,
      List<DistributorReq> distributors) {
    return new FigurineReq(
        name,
        distributors,
        tamashiiUrl,
        null,
        lineUpId,
        seriesId,
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
        null,
        null,
        null);
  }

  private CatalogContext mockCatalogRepositories() {
    CatalogContext catalogContext =
        new CatalogContext(
            loadDistributors(),
            loadDistributions(),
            loadLineups(),
            loadSeries(),
            loadGroups(),
            loadAnniversaries());

    when(distributorRepository.findAll()).thenReturn(catalogContext.distributors());
    when(distributionRepository.findAll()).thenReturn(catalogContext.distributions());
    when(lineUpRepository.findAll()).thenReturn(catalogContext.lineUps());
    when(seriesRepository.findAll()).thenReturn(catalogContext.series());
    when(groupRepository.findAll()).thenReturn(catalogContext.groups());
    when(anniversaryRepository.findAll()).thenReturn(catalogContext.anniversaries());

    return catalogContext;
  }

  private void verifyCatalogRepositoryInteractions() {
    verify(distributorRepository).findAll();
    verify(distributionRepository).findAll();
    verify(lineUpRepository).findAll();
    verify(seriesRepository).findAll();
    verify(groupRepository).findAll();
    verify(anniversaryRepository).findAll();
  }

  private List<Distributor> loadDistributors() {
    Distributor distributor1 = new Distributor();
    distributor1.setId(1L);
    distributor1.setName(DistributorName.BANDAI);
    distributor1.setCountry(JP);

    Distributor distributor2 = new Distributor();
    distributor2.setId(2L);
    distributor2.setName(DistributorName.DAM);
    distributor2.setCountry(MX);

    return List.of(distributor1, distributor2);
  }

  private List<Distribution> loadDistributions() {
    Distribution distribution1 = new Distribution();
    distribution1.setId(1L);
    distribution1.setDescription("Tamashii Web Shop");

    Distribution distribution2 = new Distribution();
    distribution2.setId(2L);
    distribution2.setDescription("Tamashii Nations");

    Distribution distribution3 = new Distribution();
    distribution3.setId(3L);
    distribution3.setDescription("Stores");

    return List.of(distribution1, distribution2, distribution3);
  }

  private List<LineUp> loadLineups() {
    LineUp lineUp1 = new LineUp();
    lineUp1.setId(1L);
    lineUp1.setDescription("Myth Cloth EX");

    LineUp lineUp2 = new LineUp();
    lineUp2.setId(2L);
    lineUp2.setDescription("Figuarts Zero Metallic Touch");

    LineUp lineUp3 = new LineUp();
    lineUp3.setId(3L);
    lineUp3.setDescription("Myth Cloth");

    LineUp lineUp4 = new LineUp();
    lineUp4.setId(4L);
    lineUp4.setDescription("DD Panoramation");

    return List.of(lineUp1, lineUp2, lineUp3, lineUp4);
  }

  private List<Series> loadSeries() {
    Series series1 = new Series();
    series1.setId(1L);
    series1.setDescription("Saint Seiya");

    Series series2 = new Series();
    series2.setId(2L);
    series2.setDescription("Saint Seiya Omega");

    return List.of(series1, series2);
  }

  private List<Group> loadGroups() {
    Group group1 = new Group();
    group1.setId(1L);
    group1.setDescription("Bronze Saint V3");

    Group group2 = new Group();
    group2.setId(2L);
    group2.setDescription("Bronze Saint V4");

    Group group3 = new Group();
    group3.setId(3L);
    group3.setDescription("Poseidon Scale");

    Group group4 = new Group();
    group4.setId(4L);
    group4.setDescription("Gold Inheritor");

    Group group5 = new Group();
    group5.setId(5L);
    group5.setDescription("God Robe");

    Group group6 = new Group();
    group6.setId(6L);
    group6.setDescription("Gold Saint");

    Group group7 = new Group();
    group7.setId(7L);
    group7.setDescription("Bronze Saint V1");

    Group group8 = new Group();
    group8.setId(8L);
    group8.setDescription("God");

    Group group9 = new Group();
    group9.setId(9L);
    group9.setDescription("Bronze Saint V2");

    Group group10 = new Group();
    group10.setId(10L);
    group10.setDescription("-");

    return List.of(group1, group2, group3, group4, group5, group6, group7, group8, group9, group10);
  }

  private List<Anniversary> loadAnniversaries() {
    Anniversary anniversary1 = new Anniversary();
    anniversary1.setId(1L);
    anniversary1.setDescription("Masami Kurumada 40th Anniversar");
    anniversary1.setYear(40);
    return List.of(anniversary1);
  }

  private Reader loadImportCsvFixture(String filename) throws IOException {
    ClassPathResource resource = new ClassPathResource("import/figurines/" + filename);
    return new InputStreamReader(resource.getInputStream());
  }
}
