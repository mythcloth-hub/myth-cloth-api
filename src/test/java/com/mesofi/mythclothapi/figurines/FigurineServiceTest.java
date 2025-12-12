package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.catalogs.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.catalogs.repository.AnniversaryRepository;
import com.mesofi.mythclothapi.catalogs.repository.DistributionRepository;
import com.mesofi.mythclothapi.catalogs.repository.GroupRepository;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.distributors.DistributorRepository;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.distributors.model.DistributorName;
import com.mesofi.mythclothapi.figurines.dto.DistributorReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineDistributorResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.mapper.CatalogContext;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapperImpl;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.utils.MethodValidationTestConfig;

import jakarta.validation.ConstraintViolationException;

@Disabled("Temporarily disabled due to refactor")
@SpringBootTest(
    classes = {FigurineService.class, MethodValidationTestConfig.class, FigurineMapperImpl.class})
public class FigurineServiceTest {

  @MockitoBean private FigurineRepository repository;
  @MockitoBean private DistributorRepository distributorRepository;
  @MockitoBean private DistributionRepository distributionRepository;
  @MockitoBean private LineUpRepository lineUpRepository;
  @MockitoBean private SeriesRepository seriesRepository;
  @MockitoBean private GroupRepository groupRepository;
  @MockitoBean private AnniversaryRepository anniversaryRepository;

  @Autowired private FigurineService service;
  @Autowired private FigurineMapper mapper;

  @Test
  void createFigurine_shouldThrowException_whenFigurineIsNull() {
    // Act + Assert
    assertThatThrownBy(() -> service.createFigurine(null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurine.request")
        .hasMessageContaining("must not be null");
  }

  @Test
  void createFigurine_shouldThrowException_whenAllFieldsAreNull() {
    // Arrange
    FigurineReq req = createFigurine(null, null, 0, 0, 0, 0, 0);

    // Act + Assert
    assertThatThrownBy(() -> service.createFigurine(req))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurine.request.name")
        .hasMessageContaining("must not be blank")
        .hasMessageContaining("createFigurine.request.distributors")
        .hasMessageContaining("must not be empty");
  }

  @ParameterizedTest
  @MethodSource("provideInvalidDistributors")
  void createFigurine_shouldThrowException_whenDistributorsAreNullOrEmpty(
      List<DistributorReq> list) {
    // Arrange
    FigurineReq req = createFigurine("Pegasus Seiya", list, 0, 0, 0, 0, 0);

    // Act + Assert
    assertThatThrownBy(() -> service.createFigurine(req))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurine.request.distributors")
        .hasMessageContaining("must not be empty");
  }

  @Test
  void createFigurine_shouldThrowException_whenDistributorsIdIsNotPositive() {
    // Arrange
    DistributorReq info = new DistributorReq(0L, null, null, null, null, null, false);
    FigurineReq req = createFigurine("Pegasus Seiya", List.of(info), 0, 0, 0, 0, 0);

    // Act + Assert
    assertThatThrownBy(() -> service.createFigurine(req))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createFigurine.request.distributors[0].distributorId")
        .hasMessageContaining("must be greater than 0");
  }

  @Test
  void createFigurine_shouldCreateNewFigurine_whenInputProvided() {
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
        new DistributorReq(
            1L,
            JPY,
            3500d,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 6, 6),
            LocalDate.of(2025, 9, 9),
            true);
    FigurineReq req = createFigurine("Pegasus Seiya", List.of(distributorReq), 1, 1, 1, 1, 1);

    Figurine figurine = mapper.toFigurine(req, catalogContext);
    figurine.setId(1L);
    when(repository.save(any(Figurine.class))).thenReturn(figurine);

    // Act
    FigurineResp figurineResp = service.createFigurine(req);

    // Assert
    assertThat(figurineResp)
        .isNotNull()
        .extracting(
            FigurineResp::id,
            FigurineResp::name,
            FigurineResp::displayableName,
            FigurineResp::distributors)
        .containsExactly(
            1L,
            "Pegasus Seiya",
            null,
            List.of(
                new FigurineDistributorResp(
                    new DistributorResp(1, "", "", "", ""),
                    JPY,
                    3500d,
                    null,
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 6, 6),
                    LocalDate.of(2025, 9, 9),
                    true)));

    verify(distributorRepository).findAll();
    verify(distributionRepository).findAll();
    verify(lineUpRepository).findAll();
    verify(seriesRepository).findAll();
    verify(groupRepository).findAll();
    verify(anniversaryRepository).findAll();
    verify(repository).save(any(Figurine.class));
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
    distribution1.setDescription("Test description");
    return List.of(distribution1);
  }

  private List<LineUp> loadLineups() {
    LineUp lineUp1 = new LineUp();
    lineUp1.setId(1L);
    lineUp1.setDescription("Test description");
    return List.of(lineUp1);
  }

  private List<Series> loadSeries() {
    Series series1 = new Series();
    series1.setId(1L);
    series1.setDescription("Test description");
    return List.of(series1);
  }

  private List<Group> loadGroups() {
    Group group1 = new Group();
    group1.setId(1L);
    group1.setDescription("Test description");
    return List.of(group1);
  }

  private List<Anniversary> loadAnniversaries() {
    Anniversary anniversary1 = new Anniversary();
    anniversary1.setId(1L);
    anniversary1.setDescription("Test description");
    anniversary1.setYear(30);
    return List.of(anniversary1);
  }

  private static Stream<Arguments> provideInvalidDistributors() {
    return Stream.of(Arguments.of((List<DistributorReq>) null), Arguments.of(List.of()));
  }

  private FigurineReq createFigurine(
      String name,
      List<DistributorReq> distributors,
      long distributionId,
      long lineupId,
      long seriesId,
      long groupId,
      long anniversaryId) {
    return new FigurineReq(
        name,
        distributors,
        null,
        distributionId,
        lineupId,
        seriesId,
        groupId,
        anniversaryId,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        null,
        null,
        null);
  }
}
