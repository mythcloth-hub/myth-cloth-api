package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import com.mesofi.mythclothapi.distributors.DistributorRepository;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurines.dto.DistributorReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineDistributorResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.mapper.CatalogContext;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.utils.MapperTestConfig;
import com.mesofi.mythclothapi.utils.MethodValidationTestConfig;

@SpringBootTest(
    classes = {FigurineService.class, MapperTestConfig.class, MethodValidationTestConfig.class})
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
    verify(figurineRepository).save(any(Figurine.class));
  }

  private List<Distributor> loadDistributors() {
    Distributor distributor1 = new Distributor();
    distributor1.setId(1L);
    distributor1.setName(DistributorName.BANDAI);
    distributor1.setCountry(JP);
    return List.of(distributor1);
  }

  private List<Distribution> loadDistributions() {
    Distribution distribution1 = new Distribution();
    distribution1.setId(1L);
    distribution1.setDescription("Tamashii Web Shop");
    return List.of(distribution1);
  }

  private List<LineUp> loadLineups() {
    LineUp lineUp1 = new LineUp();
    lineUp1.setId(1L);
    lineUp1.setDescription("Myth Cloth EX");
    return List.of(lineUp1);
  }

  private List<Series> loadSeries() {
    Series series1 = new Series();
    series1.setId(1L);
    series1.setDescription("Saint Seiya");
    return List.of(series1);
  }

  private List<Group> loadGroups() {
    Group group1 = new Group();
    group1.setId(1L);
    group1.setDescription("Bronze Saint V3");
    return List.of(group1);
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
