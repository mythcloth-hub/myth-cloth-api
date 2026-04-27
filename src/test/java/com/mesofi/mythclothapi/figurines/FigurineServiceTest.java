package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.MX;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.anniversaries.Anniversary;
import com.mesofi.mythclothapi.anniversaries.AnniversaryRepository;
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
import com.mesofi.mythclothapi.figurines.imports.FigurineCsvSource;
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

  private void mockCatalogRepositories() {
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
