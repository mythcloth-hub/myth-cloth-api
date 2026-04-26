package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.MX;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.MXN;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.USD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.anniversaries.Anniversary;
import com.mesofi.mythclothapi.anniversaries.AnniversaryRepository;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
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
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurines.dto.DistributorReq;
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
  void importFromPublicDrive_shouldCompleteSuccessfully_whenAllCatalogDataIsAvailable()
      throws IOException {
    // Arrange
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
    when(currencyRegionResolver.resolveCountry(JPY)).thenReturn(JP);
    when(currencyRegionResolver.resolveCountry(MXN)).thenReturn(MX);
    when(figurineRepository.findByLegacyName(anyString())).thenReturn(Optional.empty());
    when(figurineCsvSource.openReader()).thenReturn(loadImportCsvFixture());
    when(figurineRepository.saveAllAndFlush(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    figurineService.importFromPublicDrive();

    // Assert
    verify(distributorRepository).findAll();
    verify(distributionRepository).findAll();
    verify(lineUpRepository).findAll();
    verify(seriesRepository).findAll();
    verify(groupRepository).findAll();
    verify(anniversaryRepository).findAll();
    verify(figurineRepository).findByLegacyName("Poseidon EX OCE");
    verify(figurineRepository).findByLegacyName("Libra Shiryu ~Inheritor of the Gold Cloth~ EX");
    verify(figurineRepository).findByLegacyName("Cygnus Hyoga (God Cloth) EX");

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Figurine>> figurinesCaptor = ArgumentCaptor.forClass(List.class);

    verify(figurineRepository).saveAllAndFlush(figurinesCaptor.capture());

    List<Figurine> importedFigurines = figurinesCaptor.getValue();
    assertThat(importedFigurines).isNotNull();
    assertThat(importedFigurines.size()).isEqualTo(12);
    importedFigurines.forEach(this::assertImportedFigurineIsReadyForPersistence);

    Figurine poseidon = findImportedFigurine(importedFigurines, "Poseidon EX OCE");
    assertThat(poseidon.getNormalizedName()).isEqualTo("Poseidon");
    assertThat(poseidon.getTamashiiUrl()).isEqualTo("https://tamashiiweb.com/item/15543");
    assertThat(poseidon.getDistribution().getDescription()).isEqualTo("Tamashii Nations");
    assertThat(poseidon.getLineup().getDescription()).isEqualTo("Myth Cloth EX");
    assertThat(poseidon.getSeries().getDescription()).isEqualTo("Saint Seiya");
    assertThat(poseidon.getGroup().getDescription()).isEqualTo("Poseidon Scale");
    assertThat(poseidon.getMetalBody()).isTrue();
    assertThat(poseidon.getOce()).isTrue();
    assertThat(poseidon.getOfficialImages().size()).isEqualTo(10);
    assertThat(poseidon.getEvents().size()).isEqualTo(5);

    FigurineDistributor poseidonJapan = findDistributor(poseidon, JPY);
    assertThat(poseidonJapan.getDistributor().getCountry()).isEqualTo(JP);
    assertThat(poseidonJapan.getPrice()).isEqualTo(25000d);
    assertThat(poseidonJapan.getAnnouncementDate()).isEqualTo(LocalDate.of(2024, 11, 15));
    assertThat(poseidonJapan.getPreorderDate()).isEqualTo(LocalDate.of(2025, 8, 19));
    assertThat(poseidonJapan.getReleaseDate()).isEqualTo(LocalDate.of(2025, 11, 13));
    assertThat(poseidonJapan.isReleaseDateConfirmed()).isTrue();

    FigurineDistributor poseidonMexico = findDistributor(poseidon, MXN);
    assertThat(poseidonMexico.getDistributor().getCountry()).isEqualTo(MX);
    assertThat(poseidonMexico.getPrice()).isEqualTo(5000d);
    assertThat(poseidonMexico.getPreorderDate()).isEqualTo(LocalDate.of(2025, 10, 27));
    assertThat(poseidonMexico.getReleaseDate()).isNull();

    FigurineEvent poseidonCustomEvent =
        findEventWithDescription(poseidon, LocalDate.of(2025, 10, 27), "Special sale via");
    assertThat(poseidonCustomEvent.getFigurine()).isSameAs(poseidon);

    FigurineEvent poseidonAnnouncement =
        findEvent(poseidon, LocalDate.of(2024, 11, 15), FigurineEventType.ANNOUNCEMENT);
    assertThat(poseidonAnnouncement.getDescription())
        .isEqualTo("First announced as a possible future release.");
    assertThat(poseidonAnnouncement.getRegion()).isEqualTo(JP);

    FigurineEvent poseidonPreorder =
        findEvent(poseidon, LocalDate.of(2025, 8, 19), FigurineEventType.PREORDER_OPEN);
    assertThat(poseidonPreorder.getDescription()).isEqualTo("Pre-orders are officially open.");
    assertThat(poseidonPreorder.getRegion()).isEqualTo(JP);

    FigurineEvent poseidonRelease =
        findEvent(poseidon, LocalDate.of(2025, 11, 13), FigurineEventType.RELEASE);
    assertThat(poseidonRelease.getDescription())
        .isEqualTo("The global release date has been officially announced.");
    assertThat(poseidonRelease.getRegion()).isEqualTo(JP);

    Figurine libraShiryu =
        findImportedFigurine(importedFigurines, "Libra Shiryu ~Inheritor of the Gold Cloth~ EX");
    assertThat(libraShiryu.getDistribution().getDescription()).isEqualTo("Tamashii Web Shop");
    assertThat(libraShiryu.getGroup().getDescription()).isEqualTo("Gold Inheritor");
    assertThat(libraShiryu.getEvents().size()).isEqualTo(4);

    FigurineDistributor libraJapan = findDistributor(libraShiryu, JPY);
    assertThat(libraJapan.getPrice()).isEqualTo(24000d);
    assertThat(libraJapan.getReleaseDate()).isEqualTo(LocalDate.of(2026, 8, 1));
    assertThat(libraJapan.isReleaseDateConfirmed()).isFalse();

    FigurineEvent libraRelease =
        findEvent(libraShiryu, LocalDate.of(2026, 8, 1), FigurineEventType.RELEASE);
    assertThat(libraRelease.getRegion()).isEqualTo(JP);

    Figurine cygnusHyoga = findImportedFigurine(importedFigurines, "Cygnus Hyoga (God Cloth) EX");
    assertThat(cygnusHyoga.getDistribution()).isNull();
    assertThat(cygnusHyoga.getGroup().getDescription()).isEqualTo("Bronze Saint V4");
    assertThat(cygnusHyoga.getEvents().size()).isZero();
    assertThat(cygnusHyoga.getOfficialImages().size()).isEqualTo(1);
  }

  @Test
  void importFromPublicDrive_shouldUpdateExistingFigurine_whenLegacyNameAlreadyExists()
      throws IOException {
    // Arrange
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
    when(currencyRegionResolver.resolveCountry(JPY)).thenReturn(JP);
    when(currencyRegionResolver.resolveCountry(MXN)).thenReturn(MX);

    FigurineDistributor existingDistributor = new FigurineDistributor();
    existingDistributor.setCurrency(USD);
    existingDistributor.setPrice(100d);
    existingDistributor.setDistributor(loadDistributors().getFirst());

    FigurineEvent existingEvent = new FigurineEvent();
    existingEvent.setDescription("Existing timeline event");
    existingEvent.setEventDate(LocalDate.of(2024, 1, 1));
    existingEvent.setType(FigurineEventType.RELEASE);
    existingEvent.setRegion(MX);

    Instant originalCreationDate = Instant.parse("2024-01-01T00:00:00Z");
    Instant originalUpdateDate = Instant.parse("2024-01-02T00:00:00Z");

    Figurine existingFigurine = new Figurine();
    existingFigurine.setId(99L);
    existingFigurine.setLegacyName("Poseidon EX OCE");
    existingFigurine.setNormalizedName("Old Poseidon");
    existingFigurine.setTamashiiUrl("https://example.com/old-poseidon");
    existingFigurine.setDistribution(catalogContext.distributions().getFirst());
    existingFigurine.setLineup(catalogContext.lineUps().get(1));
    existingFigurine.setSeries(catalogContext.series().get(1));
    existingFigurine.setGroup(catalogContext.groups().getFirst());
    existingFigurine.setMetalBody(false);
    existingFigurine.setOce(false);
    existingFigurine.setRemarks("Old remarks");
    existingFigurine.setOfficialImages(
        new java.util.ArrayList<>(List.of("https://example.com/old-image.jpg")));
    existingFigurine.setDistributors(new java.util.ArrayList<>(List.of(existingDistributor)));
    existingFigurine.setEvents(new java.util.ArrayList<>(List.of(existingEvent)));
    existingFigurine.setCreationDate(originalCreationDate);
    existingFigurine.setUpdateDate(originalUpdateDate);

    when(figurineRepository.findByLegacyName(anyString()))
        .thenAnswer(
            invocation -> {
              String legacyName = invocation.getArgument(0);
              return "Poseidon EX OCE".equals(legacyName)
                  ? Optional.of(existingFigurine)
                  : Optional.empty();
            });
    when(figurineCsvSource.openReader()).thenReturn(loadImportCsvFixture());
    when(figurineRepository.saveAllAndFlush(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    figurineService.importFromPublicDrive();

    // Assert
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Figurine>> figurinesCaptor = ArgumentCaptor.forClass(List.class);

    verify(figurineRepository).saveAllAndFlush(figurinesCaptor.capture());

    Figurine updatedPoseidon = findImportedFigurine(figurinesCaptor.getValue(), "Poseidon EX OCE");

    assertThat(updatedPoseidon).isSameAs(existingFigurine);
    assertThat(updatedPoseidon.getId()).isEqualTo(99L);
    assertThat(updatedPoseidon.getCreationDate()).isEqualTo(originalCreationDate);
    assertThat(updatedPoseidon.getUpdateDate()).isAfter(originalUpdateDate);

    assertThat(updatedPoseidon.getNormalizedName()).isEqualTo("Poseidon");
    assertThat(updatedPoseidon.getTamashiiUrl()).isEqualTo("https://tamashiiweb.com/item/15543");
    assertThat(updatedPoseidon.getDistribution().getDescription()).isEqualTo("Tamashii Nations");
    assertThat(updatedPoseidon.getLineup().getDescription()).isEqualTo("Myth Cloth EX");
    assertThat(updatedPoseidon.getSeries().getDescription()).isEqualTo("Saint Seiya");
    assertThat(updatedPoseidon.getGroup().getDescription()).isEqualTo("Poseidon Scale");
    assertThat(updatedPoseidon.getMetalBody()).isTrue();
    assertThat(updatedPoseidon.getOce()).isTrue();
    assertThat(updatedPoseidon.getRemarks()).contains("Tamashii 2024");
    assertThat(updatedPoseidon.getOfficialImages().size()).isEqualTo(10);

    assertThat(updatedPoseidon.getDistributors().size()).isEqualTo(1);
    assertThat(updatedPoseidon.getDistributors().getFirst()).isSameAs(existingDistributor);
    assertThat(updatedPoseidon.getDistributors().getFirst().getCurrency()).isEqualTo(USD);
    assertThat(updatedPoseidon.getDistributors().getFirst().getPrice()).isEqualTo(100d);
    assertThat(updatedPoseidon.getDistributors().getFirst().getFigurine())
        .isSameAs(updatedPoseidon);

    assertThat(updatedPoseidon.getEvents().size()).isEqualTo(1);
    assertThat(updatedPoseidon.getEvents().getFirst()).isSameAs(existingEvent);
    assertThat(updatedPoseidon.getEvents().getFirst().getDescription())
        .isEqualTo("Existing timeline event");
    assertThat(updatedPoseidon.getEvents().getFirst().getFigurine()).isSameAs(updatedPoseidon);
  }

  @Test
  void importFromPublicDrive_shouldThrowIllegalStateException_whenCsvSourceOpenFails()
      throws IOException {
    // Arrange
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

    IOException rootCause = new IOException("boom");
    when(figurineCsvSource.openReader()).thenThrow(rootCause);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.importFromPublicDrive())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to read CSV from Google Drive")
        .hasCause(rootCause);

    verify(figurineRepository, never()).saveAllAndFlush(any());
  }

  @Test
  void createFigurine_shouldThrowException_whenFigurineIsNull() {

    // Act + Assert
    assertThatThrownBy(() -> figurineService.createFigurine(null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurine.request")
        .hasMessageContaining("must not be null");
  }

  @Test
  void createFigurine_shouldThrowException_whenAllFieldsAreNull() {
    // Arrange
    FigurineReq req = createFigurine(null, null, null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.createFigurine(req))
        .hasMessageContaining("createFigurine.request.name")
        .hasMessageContaining("must not be blank")
        .hasMessageContaining("createFigurine.request.lineUpId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void createFigurine_shouldThrowException_whenNameIsTooLong() {
    // Arrange
    FigurineReq req = createFigurine("o".repeat(101), null, null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.createFigurine(req))
        .hasMessageContaining("createFigurine.request.name")
        .hasMessageContaining("Name must not exceed 100 characters")
        .hasMessageContaining("createFigurine.request.lineUpId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void createFigurine_shouldThrowException_whenDistributorsSupplierIdIsZero() {
    // Arrange
    DistributorReq distributorReq = new DistributorReq(0L, null, -3d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.createFigurine(req))
        .hasMessageContaining("createFigurine.request.lineUpId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.distributors[0].supplierId")
        .hasMessageContaining("must be greater than 0")
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.distributors[0].price")
        .hasMessageContaining("must be greater than 0")
        .hasMessageContaining("createFigurine.request.distributors[0].currency")
        .hasMessageContaining("must not be null")
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void createFigurine_shouldThrowException_whenDistributorsPriceIsNegative() {
    // Arrange
    DistributorReq distributorReq = new DistributorReq(100L, null, -3d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.createFigurine(req))
        .hasMessageContaining("createFigurine.request.distributors[0].currency")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.lineUpId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.distributors[0].price")
        .hasMessageContaining("must be greater than 0")
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void createFigurine_shouldThrowException_whenDistributorsCurrencyIsNull() {
    // Arrange
    DistributorReq distributorReq = new DistributorReq(100L, null, 16000d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.createFigurine(req))
        .hasMessageContaining("createFigurine.request.distributors[0].currency")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.lineUpId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void createFigurine_shouldThrowException_whenLineUpIdIsNull() {
    // Arrange
    DistributorReq distributorReq = new DistributorReq(100L, JPY, 16000d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.createFigurine(req))
        .hasMessageContaining("createFigurine.request.lineUpId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void createFigurine_shouldThrowException_whenSeriesIdIsNull() {
    // Arrange
    DistributorReq distributorReq = new DistributorReq(100L, JPY, 16000d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, 200L, null, null);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.createFigurine(req))
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void createFigurine_shouldThrowException_whenUrlIsLongerThanExpected() {
    // Arrange
    DistributorReq distributorReq = new DistributorReq(100L, JPY, 16000d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, 200L, 300L, "a".repeat(70));

    // Act + Assert
    assertThatThrownBy(() -> figurineService.createFigurine(req))
        .hasMessageContaining("createFigurine.request.tamashiiUrl")
        .hasMessageContaining("Tamashii URL must not exceed 50 characters")
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void createFigurine_shouldPersistAndReturnFigurine_whenRequestIsBasicAndValid() {
    // Arrange
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

    FigurineReq req = createFigurine("Pegasus Seiya", null, 1L, 1L, null);

    Figurine figurineMapped = mapper.toFigurine(req, catalogContext);
    figurineMapped.setId(1L);

    when(figurineRepository.save(any(Figurine.class)))
        .thenAnswer(
            invocation -> {
              Figurine f = invocation.getArgument(0);
              f.setCreationDate(java.time.Instant.now());
              f.setUpdateDate(java.time.Instant.now());
              if (f.getEvents() == null) f.setEvents(new java.util.ArrayList<>());
              return f;
            });

    // Act
    FigurineResp figurineResp = figurineService.createFigurine(req);

    // Assert
    assertThat(figurineResp)
        .isNotNull()
        .extracting(
            FigurineResp::name,
            FigurineResp::displayableName,
            FigurineResp::tamashiiUrl,
            FigurineResp::releaseStatus,
            FigurineResp::lineUp,
            FigurineResp::series)
        .containsExactly(
            "Pegasus Seiya",
            figurineResp.displayableName(),
            null,
            ReleaseStatus.RUMORED,
            new CatalogResp(1, "Myth Cloth EX"),
            new CatalogResp(1, "Saint Seiya"));

    verify(distributorRepository).findAll();
    verify(distributionRepository).findAll();
    verify(lineUpRepository).findAll();
    verify(seriesRepository).findAll();
    verify(groupRepository).findAll();
    verify(anniversaryRepository).findAll();

    ArgumentCaptor<Figurine> figurineCaptor = ArgumentCaptor.forClass(Figurine.class);

    verify(figurineRepository).save(figurineCaptor.capture());

    Figurine savedFigurine = figurineCaptor.getValue();

    assertThat(savedFigurine.getUpdateDate()).isNotNull();
    assertThat(savedFigurine.getCreationDate()).isNotNull();
    assertThat(savedFigurine.getEvents()).isNotNull();
  }

  @Test
  void createFigurine_shouldPersistAndReturnFigurine_whenRequestIsValid() {
    // Arrange
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

    DistributorReq distributorReq =
        new DistributorReq(1L, CurrencyCode.JPY, 16000d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, 1L, 1L, null);

    Figurine existingFigurine = new Figurine();
    existingFigurine.setId(1L);
    existingFigurine.setNormalizedName("Pegasus Seiya");
    existingFigurine.setDistributors(new java.util.ArrayList<>());
    existingFigurine.setEvents(new java.util.ArrayList<>());

    when(figurineRepository.findById(1L)).thenReturn(Optional.of(existingFigurine));
    when(figurineRepository.save(any(Figurine.class))).thenReturn(existingFigurine);

    // Act
    FigurineResp figurineResp = figurineService.createFigurine(req);

    // Assert
    assertThat(figurineResp)
        .isNotNull()
        .extracting(
            FigurineResp::id,
            FigurineResp::name,
            FigurineResp::displayableName,
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
        .contains(
            1L,
            "Pegasus Seiya",
            null, // displayableName
            null, // tamashiiUrl
            null, // releaseStatus
            null, // distribution
            null, // lineUp
            null, // series
            null, // group
            null, // anniversary
            null, // isMetalBody
            null, // isOriginalColorEdition
            null, // isRevival
            null, // isPlainCloth
            null, // isBattleDamaged
            null, // isGoldenArmor
            null, // isGold24kEdition
            null, // isMangaVersion
            null, // isMultiPack
            null, // isArticulable
            null, // notes
            null, // officialImageUrls
            null, // unofficialImageUrls
            null, // events
            null, // createdAt
            null // updatedAt
            );
    verify(distributorRepository).findAll();
    verify(distributionRepository).findAll();
    verify(lineUpRepository).findAll();
    verify(seriesRepository).findAll();
    verify(groupRepository).findAll();
    verify(anniversaryRepository).findAll();
    ArgumentCaptor<Figurine> figurineCaptor = ArgumentCaptor.forClass(Figurine.class);
    verify(figurineRepository).save(figurineCaptor.capture());
    Figurine savedFigurine = figurineCaptor.getValue();
    assertThat(savedFigurine.getUpdateDate()).isNotNull();
    assertThat(savedFigurine.getCreationDate()).isNotNull();
    assertThat(savedFigurine.getEvents()).isNotNull();
  }

  @Test
  void importFromPublicDrive_shouldSkipDefaultEvents_whenDistributorsListIsEmpty() {
    // Arrange
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

    // Use a FigurineReq with an explicitly empty distributors list
    FigurineReq req =
        new FigurineReq(
            "Pegasus Seiya",
            List.of(),
            null,
            null,
            1L,
            1L,
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

    Figurine figurineMapped = mapper.toFigurine(req, catalogContext);
    figurineMapped.setId(1L);

    when(figurineRepository.save(any(Figurine.class))).thenReturn(figurineMapped);

    // Act
    FigurineResp figurineResp = figurineService.createFigurine(req);

    // Assert
    assertThat(figurineResp).isNotNull();
    ArgumentCaptor<Figurine> figurineCaptor = ArgumentCaptor.forClass(Figurine.class);
    verify(figurineRepository).save(figurineCaptor.capture());
    assertThat(figurineCaptor.getValue().getEvents()).isNotNull();
    assertThat(figurineCaptor.getValue().getEvents().isEmpty()).isTrue();
  }

  private void assertImportedFigurineIsReadyForPersistence(Figurine figurine) {
    assertThat(figurine.getCreationDate()).isNotNull();
    assertThat(figurine.getUpdateDate()).isNotNull();
    assertThat(figurine.getDistributors()).isNotNull();
    assertThat(figurine.getDistributors().isEmpty()).isFalse();

    figurine
        .getDistributors()
        .forEach(
            distributor -> {
              assertThat(distributor.getDistributor()).isNotNull();
              assertThat(distributor.getFigurine()).isSameAs(figurine);
            });

    figurine
        .getEvents()
        .forEach(
            event -> {
              assertThat(event.getFigurine()).isSameAs(figurine);
              assertThat(event.getRegion()).isNotNull();
            });
  }

  private Figurine findImportedFigurine(List<Figurine> figurines, String legacyName) {
    Figurine figurine =
        figurines.stream()
            .filter(item -> legacyName.equals(item.getLegacyName()))
            .findFirst()
            .orElse(null);

    assertThat(figurine).isNotNull();
    return figurine;
  }

  private FigurineDistributor findDistributor(Figurine figurine, CurrencyCode currencyCode) {
    FigurineDistributor distributor =
        figurine.getDistributors().stream()
            .filter(item -> currencyCode == item.getCurrency())
            .findFirst()
            .orElse(null);

    assertThat(distributor).isNotNull();
    return distributor;
  }

  private FigurineEvent findEvent(Figurine figurine, LocalDate date, FigurineEventType type) {
    FigurineEvent event =
        figurine.getEvents().stream()
            .filter(item -> date.equals(item.getEventDate()))
            .filter(item -> type == item.getType())
            .findFirst()
            .orElse(null);

    assertThat(event).isNotNull();
    return event;
  }

  private FigurineEvent findEventWithDescription(
      Figurine figurine, LocalDate date, String descriptionFragment) {
    FigurineEvent event =
        figurine.getEvents().stream()
            .filter(item -> date.equals(item.getEventDate()))
            .filter(item -> item.getDescription().contains(descriptionFragment))
            .findFirst()
            .orElse(null);

    assertThat(event).isNotNull();
    return event;
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

  private FigurineReq createFigurine(
      String name,
      DistributorReq distributorReq,
      Long lineUpId,
      Long seriesId,
      String tamashiiUrl) {
    return new FigurineReq(
        name,
        Objects.isNull(distributorReq) ? null : List.of(distributorReq),
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

  private Reader loadImportCsvFixture() throws IOException {
    ClassPathResource resource =
        new ClassPathResource("import/figurines/MythCloth Catalog - CatalogMyth.csv");
    return new InputStreamReader(resource.getInputStream());
  }
}
