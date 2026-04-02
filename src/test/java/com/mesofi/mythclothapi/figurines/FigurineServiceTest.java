package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.MX;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.EUR;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.MXN;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.USD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurines.dto.DistributorReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineDistributorResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.mapper.CatalogContext;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;
import com.mesofi.mythclothapi.figurines.model.Figurine;

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

  @Test
  void importFromPublicDrive_shouldCompleteSuccessfully_whenAllCatalogDataIsAvailable() {
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

    // Act
    figurineService.importFromPublicDrive();
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
        .hasMessageContaining("createFigurine.request.distributors")
        .hasMessageContaining("At least one distributor must be provided")
        .hasMessageContaining("createFigurine.request.lineUpId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.groupId")
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
        .hasMessageContaining("createFigurine.request.distributors")
        .hasMessageContaining("At least one distributor must be provided")
        .hasMessageContaining("createFigurine.request.lineUpId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.groupId")
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
        .hasMessageContaining("createFigurine.request.distributors[0].supplierId")
        .hasMessageContaining("must be greater than 0")
        .hasMessageContaining("createFigurine.request.distributors[0].price")
        .hasMessageContaining("must be greater than 0")
        .hasMessageContaining("createFigurine.request.lineUpId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.groupId")
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
        .hasMessageContaining("createFigurine.request.distributors[0].price")
        .hasMessageContaining("must be greater than 0")
        .hasMessageContaining("createFigurine.request.lineUpId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.groupId")
        .hasMessageContaining("must not be null")
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void createFigurine_shouldThrowException_whenLineUpIdIsNull() {
    // Arrange
    DistributorReq distributorReq = new DistributorReq(100L, null, 16000d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, null, null, null);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.createFigurine(req))
        .hasMessageContaining("createFigurine.request.lineUpId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.groupId")
        .hasMessageContaining("must not be null")
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void createFigurine_shouldThrowException_whenSeriesIdIsNull() {
    // Arrange
    DistributorReq distributorReq = new DistributorReq(100L, null, 16000d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, 200L, null, null);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.createFigurine(req))
        .hasMessageContaining("createFigurine.request.seriesId")
        .hasMessageContaining("must not be null")
        .hasMessageContaining("createFigurine.request.groupId")
        .hasMessageContaining("must not be null")
        .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void createFigurine_shouldThrowException_whenGroupIdIsNull() {
    // Arrange
    DistributorReq distributorReq = new DistributorReq(100L, null, 16000d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, 200L, 300L, null);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.createFigurine(req))
        .hasMessageContaining("createFigurine.request.groupId")
        .hasMessageContaining("must not be null")
        .isInstanceOf(ConstraintViolationException.class);
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
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, 1L, 1L, 1L);

    Figurine figurineMapped = mapper.toFigurine(req, catalogContext);
    figurineMapped.setId(1L);

    when(figurineRepository.save(any(Figurine.class))).thenReturn(figurineMapped);

    // Act
    FigurineResp figurineResp = figurineService.createFigurine(req);

    // Assert
    assertThat(figurineResp)
        .isNotNull()
        .extracting(
            FigurineResp::id,
            FigurineResp::name,
            FigurineResp::displayableName,
            FigurineResp::distributors,
            FigurineResp::tamashiiUrl,
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
            1L,
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
            null,
            null,
            new CatalogResp(1, "Myth Cloth EX"),
            new CatalogResp(1, "Saint Seiya"),
            new CatalogResp(1, "Bronze Saint V3"),
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
  void
      createFigurine_shouldCreateDistributorEventsAndSetBackReferences_whenDistributorDatesAreProvided() {
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

    DistributorReq distributorReq =
        new DistributorReq(
            1L,
            CurrencyCode.JPY,
            16000d,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 2, 2),
            LocalDate.of(2026, 3, 3),
            null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, 1L, 1L, 1L);

    Figurine figurineMapped = mapper.toFigurine(req, catalogContext);
    figurineMapped.setId(1L);

    when(figurineRepository.save(any(Figurine.class))).thenReturn(figurineMapped);

    // Act
    FigurineResp figurineResp = figurineService.createFigurine(req);

    // Assert
    assertThat(figurineResp)
        .isNotNull()
        .extracting(
            FigurineResp::id,
            FigurineResp::name,
            FigurineResp::displayableName,
            FigurineResp::distributors,
            FigurineResp::tamashiiUrl,
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
            1L,
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
                    false)),
            null,
            null,
            new CatalogResp(1, "Myth Cloth EX"),
            new CatalogResp(1, "Saint Seiya"),
            new CatalogResp(1, "Bronze Saint V3"),
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

    verify(distributorRepository).findAll();
    verify(distributionRepository).findAll();
    verify(lineUpRepository).findAll();
    verify(seriesRepository).findAll();
    verify(groupRepository).findAll();
    verify(anniversaryRepository).findAll();

    ArgumentCaptor<Figurine> figurineCaptor = ArgumentCaptor.forClass(Figurine.class);

    verify(figurineRepository).save(figurineCaptor.capture());

    Figurine figurineToBeSaved = figurineCaptor.getValue();

    assertThat(figurineToBeSaved.getUpdateDate()).isNotNull();
    assertThat(figurineToBeSaved.getCreationDate()).isNotNull();

    assertThat(figurineToBeSaved.getEvents().size()).isEqualTo(3);
    assertThat(figurineToBeSaved.getEvents().getFirst().getDescription())
        .isEqualTo("First announced as a possible future release.");
    assertThat(figurineToBeSaved.getEvents().getFirst().getEventDate())
        .isEqualTo(LocalDate.of(2026, 1, 1));
    assertThat(figurineToBeSaved.getEvents().getFirst().getType())
        .isEqualTo(FigurineEventType.ANNOUNCEMENT);
    assertThat(figurineToBeSaved.getEvents().getFirst().getRegion()).isEqualTo(JP);
    assertThat(figurineToBeSaved.getEvents().get(1).getDescription())
        .isEqualTo("Pre-orders are officially open.");
    assertThat(figurineToBeSaved.getEvents().get(1).getEventDate())
        .isEqualTo(LocalDate.of(2026, 2, 2));
    assertThat(figurineToBeSaved.getEvents().get(1).getType())
        .isEqualTo(FigurineEventType.PREORDER_OPEN);
    assertThat(figurineToBeSaved.getEvents().get(1).getRegion()).isEqualTo(JP);
    assertThat(figurineToBeSaved.getEvents().get(2).getDescription())
        .isEqualTo("The global release date has been officially announced.");
    assertThat(figurineToBeSaved.getEvents().get(2).getEventDate())
        .isEqualTo(LocalDate.of(2026, 3, 3));
    assertThat(figurineToBeSaved.getEvents().get(2).getType()).isEqualTo(FigurineEventType.RELEASE);
    assertThat(figurineToBeSaved.getEvents().get(2).getRegion()).isEqualTo(JP);
    figurineToBeSaved
        .getEvents()
        .forEach(figurineEvent -> assertThat(figurineEvent.getFigurine()).isNotNull());

    assertThat(figurineToBeSaved.getDistributors().size()).isEqualTo(1);
    figurineToBeSaved
        .getDistributors()
        .forEach(distributor -> assertThat(distributor.getFigurine()).isNotNull());
  }

  @Test
  void readFigurine_shouldThrowException_whenFigurineDoesNotExist() {
    // Arrange
    when(figurineRepository.findById(0L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> figurineService.readFigurine(0L))
        .isInstanceOf(FigurineNotFoundException.class)
        .hasMessageContaining("Figurine not found")
        .extracting(ex -> ((FigurineNotFoundException) ex).getId())
        .isEqualTo(0L);

    verify(figurineRepository).findById(0L);
  }

  @Test
  void readFigurine_shouldReturnFigurine_whenFigurineExists() {
    // Arrange
    Distributor distributor = loadDistributors().getFirst();

    FigurineDistributor figurineDistributor = new FigurineDistributor();
    figurineDistributor.setDistributor(distributor);
    figurineDistributor.setCurrency(JPY);
    figurineDistributor.setPrice(16000d);
    figurineDistributor.setReleaseDate(LocalDate.of(2026, 3, 3));

    Figurine figurine = new Figurine();
    figurine.setId(1L);
    figurine.setNormalizedName("Pegasus Seiya");
    figurine.setDistributors(List.of(figurineDistributor));
    figurine.setEvents(List.of());
    figurine.setCreationDate(Instant.parse("2026-01-01T00:00:00Z"));
    figurine.setUpdateDate(Instant.parse("2026-01-02T00:00:00Z"));

    when(figurineRepository.findById(1L)).thenReturn(Optional.of(figurine));

    // Act
    FigurineResp figurineResp = figurineService.readFigurine(1L);

    // Assert
    assertThat(figurineResp)
        .isNotNull()
        .extracting(FigurineResp::id, FigurineResp::name, FigurineResp::displayableName)
        .containsExactly(1L, "Pegasus Seiya", "FIXME");

    assertThat(figurineResp.distributors().size()).isEqualTo(1);

    FigurineDistributorResp distributorResp = figurineResp.distributors().getFirst();
    assertThat(distributorResp)
        .extracting(
            FigurineDistributorResp::currency,
            FigurineDistributorResp::price,
            FigurineDistributorResp::priceWithTax)
        .containsExactly(JPY, 16000d, 17600d);

    assertThat(figurineResp.createdAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    assertThat(figurineResp.updatedAt()).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"));

    verify(figurineRepository).findById(1L);
  }

  @Test
  void readFigurines_shouldReturnMappedPage_whenFigurinesExist() {
    // Arrange
    int page = 0;
    int size = 2;
    PageRequest pageRequest = PageRequest.of(page, size);

    Distributor distributor = loadDistributors().getFirst();

    FigurineDistributor figurineDistributor1 = new FigurineDistributor();
    figurineDistributor1.setDistributor(distributor);
    figurineDistributor1.setCurrency(JPY);
    figurineDistributor1.setPrice(16000d);
    figurineDistributor1.setReleaseDate(LocalDate.of(2026, 3, 3));

    Figurine figurine1 = new Figurine();
    figurine1.setId(1L);
    figurine1.setNormalizedName("Pegasus Seiya");
    figurine1.setDistributors(List.of(figurineDistributor1));
    figurine1.setEvents(List.of());
    figurine1.setCreationDate(Instant.parse("2026-01-01T00:00:00Z"));
    figurine1.setUpdateDate(Instant.parse("2026-01-02T00:00:00Z"));

    FigurineDistributor figurineDistributor2 = new FigurineDistributor();
    figurineDistributor2.setDistributor(distributor);
    figurineDistributor2.setCurrency(JPY);
    figurineDistributor2.setPrice(20000d);
    figurineDistributor2.setReleaseDate(LocalDate.of(2026, 3, 3));

    Figurine figurine2 = new Figurine();
    figurine2.setId(2L);
    figurine2.setNormalizedName("Dragon Shiryu");
    figurine2.setDistributors(List.of(figurineDistributor2));
    figurine2.setEvents(List.of());
    figurine2.setCreationDate(Instant.parse("2026-01-03T00:00:00Z"));
    figurine2.setUpdateDate(Instant.parse("2026-01-04T00:00:00Z"));

    Page<Figurine> figurinePage = new PageImpl<>(List.of(figurine1, figurine2), pageRequest, 5);
    when(figurineRepository.findAll(pageRequest)).thenReturn(figurinePage);

    // Act
    Page<FigurineResp> responsePage = figurineService.readFigurines(page, size);

    // Assert
    assertThat(responsePage).isNotNull();
    assertThat(responsePage.getNumber()).isEqualTo(0);
    assertThat(responsePage.getSize()).isEqualTo(2);
    assertThat(responsePage.getTotalElements()).isEqualTo(5);
    assertThat(responsePage.getContent().size()).isEqualTo(2);

    FigurineResp first = responsePage.getContent().getFirst();
    assertThat(first)
        .extracting(FigurineResp::id, FigurineResp::name, FigurineResp::displayableName)
        .containsExactly(1L, "Pegasus Seiya", "FIXME");
    assertThat(first.distributors().getFirst().priceWithTax()).isEqualTo(17600d);

    FigurineResp second = responsePage.getContent().get(1);
    assertThat(second)
        .extracting(FigurineResp::id, FigurineResp::name, FigurineResp::displayableName)
        .containsExactly(2L, "Dragon Shiryu", "FIXME");
    assertThat(second.distributors().getFirst().priceWithTax()).isEqualTo(22000d);

    verify(figurineRepository).findAll(pageRequest);
  }

  @Test
  void readFigurines_shouldReturnEmptyPage_whenNoFigurinesExist() {
    // Arrange
    int page = 1;
    int size = 10;
    PageRequest pageRequest = PageRequest.of(page, size);

    when(figurineRepository.findAll(pageRequest)).thenReturn(Page.empty(pageRequest));

    // Act
    Page<FigurineResp> responsePage = figurineService.readFigurines(page, size);

    // Assert
    assertThat(responsePage).isNotNull();
    assertThat(responsePage.getNumber()).isEqualTo(1);
    assertThat(responsePage.getSize()).isEqualTo(10);
    assertThat(responsePage.getTotalElements()).isZero();
    assertThat(responsePage.getContent().size()).isZero();

    verify(figurineRepository).findAll(pageRequest);
  }

  @Test
  void readFigurines_shouldThrowException_whenPageIsNegative() {
    // Act + Assert
    assertThatThrownBy(() -> figurineService.readFigurines(-1, 10))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Page index must not be less than zero");

    verify(figurineRepository, never()).findAll(any(PageRequest.class));
  }

  @Test
  void readFigurines_shouldThrowException_whenSizeIsZero() {
    // Act + Assert
    assertThatThrownBy(() -> figurineService.readFigurines(0, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Page size must not be less than one");

    verify(figurineRepository, never()).findAll(any(PageRequest.class));
  }

  @Test
  void updateFigurine_shouldThrowException_whenFigurineDoesNotExist() {
    // Arrange
    when(figurineRepository.findById(99L)).thenReturn(Optional.empty());

    DistributorReq distributorReq =
        new DistributorReq(1L, CurrencyCode.JPY, 16000d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, 1L, 1L, 1L);

    // Act + Assert
    assertThatThrownBy(() -> figurineService.updateFigurine(99L, req))
        .isInstanceOf(FigurineNotFoundException.class)
        .hasMessageContaining("Figurine not found")
        .extracting(ex -> ((FigurineNotFoundException) ex).getId())
        .isEqualTo(99L);

    verify(figurineRepository).findById(99L);
  }

  @Test
  void updateFigurine_shouldUpdateAndReturnFigurine_whenRequestIsValid() {
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

    Distributor distributor = loadDistributors().getFirst();

    FigurineDistributor existingDistributor = new FigurineDistributor();
    existingDistributor.setDistributor(distributor);
    existingDistributor.setCurrency(JPY);
    existingDistributor.setPrice(16000d);

    Figurine existingFigurine = new Figurine();
    existingFigurine.setId(1L);
    existingFigurine.setNormalizedName("Pegasus Seiya");
    existingFigurine.setDistributors(new java.util.ArrayList<>(List.of(existingDistributor)));
    existingFigurine.setEvents(new java.util.ArrayList<>());
    existingFigurine.setCreationDate(Instant.parse("2026-01-01T00:00:00Z"));

    when(figurineRepository.findById(1L)).thenReturn(Optional.of(existingFigurine));
    when(figurineRepository.save(any(Figurine.class))).thenReturn(existingFigurine);

    DistributorReq distributorReq =
        new DistributorReq(1L, CurrencyCode.JPY, 18000d, null, null, null, null);
    FigurineReq req = createFigurine("Dragon Shiryu", distributorReq, 1L, 1L, 1L);

    // Act
    FigurineResp figurineResp = figurineService.updateFigurine(1L, req);

    // Assert
    assertThat(figurineResp).isNotNull();
    assertThat(figurineResp.id()).isEqualTo(1L);
    assertThat(figurineResp.name()).isEqualTo("Dragon Shiryu");
    assertThat(figurineResp.distributors().size()).isEqualTo(1);
    assertThat(figurineResp.distributors().getFirst().price()).isEqualTo(18000d);
    assertThat(figurineResp.distributors().getFirst().priceWithTax()).isEqualTo(18000d);

    ArgumentCaptor<Figurine> figurineCaptor = ArgumentCaptor.forClass(Figurine.class);
    verify(figurineRepository).save(figurineCaptor.capture());
    assertThat(figurineCaptor.getValue().getCreationDate())
        .isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));

    verify(figurineRepository).findById(1L);
    verify(distributorRepository).findAll();
    verify(distributionRepository).findAll();
    verify(lineUpRepository).findAll();
    verify(seriesRepository).findAll();
    verify(groupRepository).findAll();
    verify(anniversaryRepository).findAll();
  }

  @Test
  void updateFigurine_shouldUpdateExistingDistributor_whenCurrencyCodeMatches() {
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

    Distributor distributor = loadDistributors().getFirst();

    FigurineDistributor existingDistributor = new FigurineDistributor();
    existingDistributor.setDistributor(distributor);
    existingDistributor.setCurrency(JPY);
    existingDistributor.setPrice(16000d);

    Figurine existingFigurine = new Figurine();
    existingFigurine.setId(1L);
    existingFigurine.setNormalizedName("Pegasus Seiya");
    existingFigurine.setDistributors(new java.util.ArrayList<>(List.of(existingDistributor)));
    existingFigurine.setEvents(new java.util.ArrayList<>());

    when(figurineRepository.findById(1L)).thenReturn(Optional.of(existingFigurine));
    when(figurineRepository.save(any(Figurine.class))).thenReturn(existingFigurine);

    DistributorReq distributorReq =
        new DistributorReq(1L, CurrencyCode.JPY, 20000d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, 1L, 1L, 1L);

    // Act
    figurineService.updateFigurine(1L, req);

    // Assert
    ArgumentCaptor<Figurine> figurineCaptor = ArgumentCaptor.forClass(Figurine.class);
    verify(figurineRepository).save(figurineCaptor.capture());

    Figurine savedFigurine = figurineCaptor.getValue();
    assertThat(savedFigurine.getDistributors().size()).isEqualTo(1);
    assertThat(savedFigurine.getDistributors().getFirst().getCurrency()).isEqualTo(JPY);
    assertThat(savedFigurine.getDistributors().getFirst().getPrice()).isEqualTo(20000d);
  }

  @Test
  void updateFigurine_shouldAddNewDistributor_whenCurrencyCodeIsNew() {
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

    Distributor distributor = loadDistributors().getFirst();

    FigurineDistributor existingDistributor = new FigurineDistributor();
    existingDistributor.setDistributor(distributor);
    existingDistributor.setCurrency(JPY);
    existingDistributor.setPrice(16000d);

    Figurine existingFigurine = new Figurine();
    existingFigurine.setId(1L);
    existingFigurine.setNormalizedName("Pegasus Seiya");
    existingFigurine.setDistributors(new java.util.ArrayList<>(List.of(existingDistributor)));
    existingFigurine.setEvents(new java.util.ArrayList<>());

    when(figurineRepository.findById(1L)).thenReturn(Optional.of(existingFigurine));
    when(figurineRepository.save(any(Figurine.class))).thenReturn(existingFigurine);

    DistributorReq distributorReq =
        new DistributorReq(1L, CurrencyCode.USD, 150d, null, null, null, null);
    FigurineReq req = createFigurine("Pegasus Seiya", distributorReq, 1L, 1L, 1L);

    // Act
    figurineService.updateFigurine(1L, req);

    // Assert
    ArgumentCaptor<Figurine> figurineCaptor = ArgumentCaptor.forClass(Figurine.class);
    verify(figurineRepository).save(figurineCaptor.capture());

    Figurine savedFigurine = figurineCaptor.getValue();
    assertThat(savedFigurine.getDistributors().size()).isEqualTo(2);
    assertThat(savedFigurine.getDistributors().get(1).getCurrency()).isEqualTo(USD);
    assertThat(savedFigurine.getDistributors().get(1).getPrice()).isEqualTo(150d);
    assertThat(savedFigurine.getDistributors().get(1).getFigurine()).isNotNull();
  }

  @Test
  void deleteFigurine_shouldThrowException_whenFigurineDoesNotExist() {
    // Arrange
    when(figurineRepository.findById(99L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> figurineService.deleteFigurine(99L))
        .isInstanceOf(FigurineNotFoundException.class)
        .hasMessageContaining("Figurine not found")
        .extracting(ex -> ((FigurineNotFoundException) ex).getId())
        .isEqualTo(99L);

    verify(figurineRepository).findById(99L);
    verify(figurineRepository, never()).delete(any(Figurine.class));
  }

  @Test
  void deleteFigurine_shouldDeleteFigurine_whenFigurineExists() {
    // Arrange
    Figurine existingFigurine = new Figurine();
    existingFigurine.setId(1L);
    existingFigurine.setNormalizedName("Pegasus Seiya");
    existingFigurine.setDistributors(List.of());
    existingFigurine.setEvents(List.of());

    when(figurineRepository.findById(1L)).thenReturn(Optional.of(existingFigurine));

    // Act
    figurineService.deleteFigurine(1L);

    // Assert
    verify(figurineRepository).findById(1L);
    verify(figurineRepository).delete(existingFigurine);
  }

  @Test
  void calculatePriceWithTax_shouldReturnNull_whenDistributorIsNull() {
    // Act
    Double priceWithTax = figurineService.calculatePriceWithTax(null);

    // Assert
    assertThat(priceWithTax).isNull();
  }

  @Test
  void calculatePriceWithTax_shouldReturnNull_whenPriceIsNullOrNonPositive() {
    // Assert all invalid price inputs in one test to keep behavior coverage concise.
    for (Double price : java.util.Arrays.asList(null, -4d, 0d)) {
      FigurineDistributor figurineDistributor = new FigurineDistributor();
      figurineDistributor.setPrice(price);

      Double priceWithTax = figurineService.calculatePriceWithTax(figurineDistributor);

      assertThat(priceWithTax).isNull();
    }
  }

  @Test
  void calculatePriceWithTax_shouldReturnPriceWithTax_whenCurrencyIsJPYAndReleaseDateIsNull() {
    // Arrange
    FigurineDistributor figurineDistributor = new FigurineDistributor();
    figurineDistributor.setCurrency(JPY);
    figurineDistributor.setPrice(16000d);

    // Act
    Double priceWithTax = figurineService.calculatePriceWithTax(figurineDistributor);

    // Assert
    assertThat(priceWithTax).isEqualTo(16000d);
  }

  @Test
  void calculatePriceWithTax_shouldReturnPriceWithTax_whenCurrencyIsJPYAndReleaseDateBefore1997() {
    // Arrange
    FigurineDistributor figurineDistributor = new FigurineDistributor();
    figurineDistributor.setCurrency(JPY);
    figurineDistributor.setPrice(16000d);
    figurineDistributor.setReleaseDate(LocalDate.of(1995, 1, 1));

    // Act
    Double priceWithTax = figurineService.calculatePriceWithTax(figurineDistributor);

    // Assert
    assertThat(priceWithTax).isEqualTo(16480d);
  }

  @Test
  void calculatePriceWithTax_shouldReturnPriceWithTax_whenCurrencyIsJPYAndReleaseDateBefore2014() {
    // Arrange
    FigurineDistributor figurineDistributor = new FigurineDistributor();
    figurineDistributor.setCurrency(JPY);
    figurineDistributor.setPrice(16000d);
    figurineDistributor.setReleaseDate(LocalDate.of(2014, 1, 1));

    // Act
    Double priceWithTax = figurineService.calculatePriceWithTax(figurineDistributor);

    // Assert
    assertThat(priceWithTax).isEqualTo(16800d);
  }

  @Test
  void calculatePriceWithTax_shouldReturnPriceWithTax_whenCurrencyIsJPYAndReleaseDateBefore2019() {
    // Arrange
    FigurineDistributor figurineDistributor = new FigurineDistributor();
    figurineDistributor.setCurrency(JPY);
    figurineDistributor.setPrice(16000d);
    figurineDistributor.setReleaseDate(LocalDate.of(2019, 1, 1));

    // Act
    Double priceWithTax = figurineService.calculatePriceWithTax(figurineDistributor);

    // Assert
    assertThat(priceWithTax).isEqualTo(17280d);
  }

  @Test
  void calculatePriceWithTax_shouldReturnPriceWithTax_whenCurrencyIsJPYAndReleaseDateAfter2019() {
    // Arrange
    FigurineDistributor figurineDistributor = new FigurineDistributor();
    figurineDistributor.setCurrency(JPY);
    figurineDistributor.setPrice(16000d);
    figurineDistributor.setReleaseDate(LocalDate.of(2026, 1, 1));

    // Act
    Double priceWithTax = figurineService.calculatePriceWithTax(figurineDistributor);

    // Assert
    assertThat(priceWithTax).isEqualTo(17600d);
  }

  @Test
  void calculatePriceWithTax_shouldReturnPriceWithTax_whenCurrencyIsMXN() {
    // Arrange
    FigurineDistributor figurineDistributor = new FigurineDistributor();
    figurineDistributor.setCurrency(MXN);
    figurineDistributor.setPrice(2500d);

    // Act
    Double priceWithTax = figurineService.calculatePriceWithTax(figurineDistributor);

    // Assert
    assertThat(priceWithTax).isEqualTo(2900);
  }

  @Test
  void calculatePriceWithTax_shouldReturnPriceWithTax_whenCurrencyIsUSD() {
    // Arrange
    FigurineDistributor figurineDistributor = new FigurineDistributor();
    figurineDistributor.setCurrency(USD);
    figurineDistributor.setPrice(150d);

    // Act
    Double priceWithTax = figurineService.calculatePriceWithTax(figurineDistributor);

    // Assert
    assertThat(priceWithTax).isEqualTo(150);
  }

  @Test
  void calculatePriceWithTax_shouldReturnPriceWithTax_whenCurrencyIsEUR() {
    // Arrange
    FigurineDistributor figurineDistributor = new FigurineDistributor();
    figurineDistributor.setCurrency(EUR);
    figurineDistributor.setPrice(150d);

    // Act
    Double priceWithTax = figurineService.calculatePriceWithTax(figurineDistributor);

    // Assert
    assertThat(priceWithTax).isEqualTo(150);
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
      String name, DistributorReq distributorReq, Long lineUpId, Long seriesId, Long groupId) {
    return new FigurineReq(
        name,
        Objects.isNull(distributorReq) ? null : List.of(distributorReq),
        null,
        null,
        lineUpId,
        seriesId,
        groupId,
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
}
