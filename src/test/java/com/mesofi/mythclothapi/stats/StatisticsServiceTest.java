package com.mesofi.mythclothapi.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mesofi.mythclothapi.anniversaries.AnniversaryRepository;
import com.mesofi.mythclothapi.anniversaries.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.catalogs.repository.GroupRepository;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.FigurineService;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;
import com.mesofi.mythclothapi.stats.dto.FigurineByMonthResp;
import com.mesofi.mythclothapi.stats.dto.MonthStatisticsResp;
import com.mesofi.mythclothapi.stats.dto.YearStatisticsResp;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

  private static final FigurineFilter EMPTY_FILTER =
      new FigurineFilter(
          null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
          null);

  @InjectMocks private StatisticsService service;

  @Mock private FigurineService figurineService;
  @Mock private FigurineRepository repository;
  @Mock private LineUpRepository lineUpRepository;
  @Mock private SeriesRepository seriesRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private AnniversaryRepository anniversaryRepository;

  @Test
  void retrieveStatistics_shouldAggregateTotalsByCatalogAndStatus() {
    LineUp ex = createLineUp(1L, "Myth Cloth EX");
    LineUp myth = createLineUp(2L, "Myth Cloth");

    Series hades = createSeries(11L, "Hades");
    Series sanctuary = createSeries(12L, "Sanctuary");

    Group bronze = createGroup(21L, "Bronze");
    Group gold = createGroup(22L, "Gold");

    Anniversary tenth = createAnniversary(31L, "10th Anniversary");
    Anniversary twentieth = createAnniversary(32L, "20th Anniversary");

    Figurine andromeda = createFigurine(1L, "Andromeda Shun", ex, "https://img/shun.jpg");
    andromeda.setSeries(hades);
    andromeda.setGroup(bronze);
    andromeda.setAnniversary(tenth);

    Figurine phoenix = createFigurine(2L, "Phoenix Ikki", ex, "https://img/ikki.jpg");
    phoenix.setSeries(sanctuary);
    phoenix.setGroup(gold);
    phoenix.setAnniversary(twentieth);

    Figurine pegasus = createFigurine(3L, "Pegasus Seiya", myth, "https://img/seiya.jpg");
    pegasus.setSeries(hades);
    pegasus.setGroup(bronze);
    pegasus.setAnniversary(tenth);

    when(repository.findAll(EMPTY_FILTER)).thenReturn(List.of(andromeda, phoenix, pegasus));
    when(lineUpRepository.findAll()).thenReturn(List.of(ex, myth));
    when(seriesRepository.findAll()).thenReturn(List.of(hades, sanctuary));
    when(groupRepository.findAll()).thenReturn(List.of(bronze, gold));
    when(anniversaryRepository.findAll()).thenReturn(List.of(tenth, twentieth));

    when(figurineService.calculateReleaseStatus(andromeda)).thenReturn(ReleaseStatus.RELEASED);
    when(figurineService.calculateReleaseStatus(phoenix)).thenReturn(ReleaseStatus.ANNOUNCED);
    when(figurineService.calculateReleaseStatus(pegasus)).thenReturn(ReleaseStatus.RELEASED);

    var result = service.retrieveStatistics(EMPTY_FILTER);

    assertThat(result.totalFigurines()).isEqualTo(3);
    assertThat(result.countByLineUp())
        .containsExactlyInAnyOrderEntriesOf(Map.of("Myth Cloth EX", 2, "Myth Cloth", 1));
    assertThat(result.countBySeries())
        .containsExactlyInAnyOrderEntriesOf(Map.of("Hades", 2, "Sanctuary", 1));
    assertThat(result.countByGroup())
        .containsExactlyInAnyOrderEntriesOf(Map.of("Bronze", 2, "Gold", 1));
    assertThat(result.countByAnniversary())
        .containsExactlyInAnyOrderEntriesOf(Map.of("10th Anniversary", 2, "20th Anniversary", 1));
    assertThat(result.totalByReleaseStatus())
        .containsExactlyInAnyOrderEntriesOf(Map.of("RELEASED", 2, "ANNOUNCED", 1));

    verify(repository).findAll(EMPTY_FILTER);
  }

  @Test
  void retrieveStatisticsByReleases_shouldCountDistinctYearsForReleasedAndAnnounced() {
    LineUp ex = createLineUp(1L, "Myth Cloth EX");
    LineUp myth = createLineUp(2L, "Myth Cloth");

    Figurine camus = createFigurine(10L, "Aquarius Camus", ex, "https://img/camus.jpg");
    camus.setDistributors(
        List.of(
            createDistributor(LocalDate.of(2024, 1, 10)),
            createDistributor(LocalDate.of(2024, 5, 20)),
            createDistributor(LocalDate.of(2025, 2, 10))));

    Figurine hyoga = createFigurine(11L, "Cygnus Hyoga", ex, "https://img/hyoga.jpg");
    hyoga.setDistributors(List.of(createDistributor(LocalDate.of(2025, 7, 15))));

    Figurine saga = createFigurine(12L, "Gemini Saga", myth, "https://img/saga.jpg");
    saga.setDistributors(List.of(createDistributor(LocalDate.of(2025, 3, 1))));

    Figurine aiolia = createFigurine(13L, "Leo Aiolia", myth, "https://img/aiolia.jpg");
    aiolia.setDistributors(List.of(createDistributor(null)));

    when(repository.findAll(EMPTY_FILTER)).thenReturn(List.of(camus, hyoga, saga, aiolia));
    when(lineUpRepository.findAll()).thenReturn(List.of(ex, myth));

    when(figurineService.calculateReleaseStatus(camus)).thenReturn(ReleaseStatus.RELEASED);
    when(figurineService.calculateReleaseStatus(hyoga)).thenReturn(ReleaseStatus.ANNOUNCED);
    when(figurineService.calculateReleaseStatus(saga)).thenReturn(ReleaseStatus.PROTOTYPE);
    when(figurineService.calculateReleaseStatus(aiolia)).thenReturn(ReleaseStatus.RELEASED);

    List<YearStatisticsResp> result = service.retrieveStatisticsByReleases(EMPTY_FILTER);

    YearStatisticsResp stats2024 =
        result.stream().filter(r -> r.year() == 2024).findFirst().orElseThrow();
    YearStatisticsResp stats2025 =
        result.stream().filter(r -> r.year() == 2025).findFirst().orElseThrow();

    assertThat(stats2024.lineUp())
        .extracting("line", "count")
        .containsExactly(tuple("Myth Cloth EX", 1));
    assertThat(stats2025.lineUp())
        .extracting("line", "count")
        .containsExactly(tuple("Myth Cloth EX", 2));
    assertThat(result).allSatisfy(year -> assertThat(year.year()).isGreaterThanOrEqualTo(2003));

    verify(repository).findAll(EMPTY_FILTER);
  }

  @Test
  void retrieveStatisticsByReleases_shouldIncludeStartingYearAndNextYearOnly() {
    int nextYear = LocalDate.now().getYear() + 1;
    LineUp ex = createLineUp(1L, "Myth Cloth EX");

    Figurine shaka = createFigurine(15L, "Virgo Shaka", ex, "https://img/shaka.jpg");
    shaka.setDistributors(
        List.of(
            createDistributor(LocalDate.of(2002, 1, 1)),
            createDistributor(LocalDate.of(2003, 2, 2)),
            createDistributor(LocalDate.of(nextYear, 3, 3)),
            createDistributor(LocalDate.of(nextYear + 1, 4, 4))));

    when(repository.findAll(EMPTY_FILTER)).thenReturn(List.of(shaka));
    when(lineUpRepository.findAll()).thenReturn(List.of(ex));
    when(figurineService.calculateReleaseStatus(shaka)).thenReturn(ReleaseStatus.RELEASED);

    List<YearStatisticsResp> result = service.retrieveStatisticsByReleases(EMPTY_FILTER);

    YearStatisticsResp stats2003 =
        result.stream().filter(resp -> resp.year() == 2003).findFirst().orElseThrow();
    YearStatisticsResp statsNextYear =
        result.stream().filter(resp -> resp.year() == nextYear).findFirst().orElseThrow();

    assertThat(stats2003.lineUp())
        .extracting("line", "count")
        .containsExactly(tuple("Myth Cloth EX", 1));
    assertThat(statsNextYear.lineUp())
        .extracting("line", "count")
        .containsExactly(tuple("Myth Cloth EX", 1));
    assertThat(result).extracting(YearStatisticsResp::year).doesNotContain(nextYear + 1);
    assertThat(result.getLast().year()).isEqualTo(nextYear);
  }

  @Test
  void retrieveStatisticsByYear_shouldGroupByMonthAndLineUp_withSortingAndFallbacks() {
    LineUp bronze = createLineUp(1L, "Bronze");
    LineUp gold = createLineUp(2L, "Gold");

    Figurine phoenix = createFigurine(44L, "Phoenix Ikki", bronze, "https://img/phoenix.jpg");
    phoenix.setDistributors(List.of(createDistributor(LocalDate.of(2026, 1, 22))));

    Figurine andromeda = createFigurine(42L, "Andromeda Shun", bronze, null);
    andromeda.setOfficialImages(List.of());
    andromeda.setDistributors(
        List.of(
            createDistributor(LocalDate.of(2026, 1, 10)),
            createDistributor(LocalDate.of(2026, 2, 10))));

    Figurine virgo = createFigurine(36L, "Virgo Shaka", gold, "https://img/virgo.jpg");
    virgo.setDistributors(List.of(createDistributor(LocalDate.of(2026, 2, 2))));

    Figurine unknown = createFigurine(50L, "Unknown Fighter", null, "https://img/unknown.jpg");
    unknown.setDistributors(List.of(createDistributor(LocalDate.of(2026, 1, 15))));

    Figurine skipped = createFigurine(51L, "Skipped", gold, "https://img/skipped.jpg");
    skipped.setDistributors(List.of(createDistributor(LocalDate.of(2025, 12, 12))));

    when(repository.findAllByYear(2026))
        .thenReturn(List.of(phoenix, virgo, andromeda, unknown, skipped));

    List<MonthStatisticsResp> result = service.retrieveStatisticsByYear(2026);

    assertThat(result).hasSize(2);
    assertThat(result).extracting(MonthStatisticsResp::month).containsExactly(1, 2);

    MonthStatisticsResp january = result.getFirst();
    assertThat(january.month()).isEqualTo(1);
    assertThat(january.name()).isEqualTo("January");
    assertThat(january.lineUp()).hasSize(2);
    assertThat(january.lineUp().getFirst().line()).isEqualTo("Bronze");
    assertThat(january.lineUp().get(1).line()).isEqualTo("Unknown");
    assertThat(january.lineUp().getFirst().figurines())
        .extracting(FigurineByMonthResp::id, FigurineByMonthResp::name, FigurineByMonthResp::url)
        .containsExactly(
            tuple(42L, "Andromeda Shun", ""),
            tuple(44L, "Phoenix Ikki", "https://img/phoenix.jpg"));
    assertThat(january.lineUp().get(1).figurines())
        .extracting(FigurineByMonthResp::id, FigurineByMonthResp::name, FigurineByMonthResp::url)
        .containsExactly(tuple(50L, "Unknown Fighter", "https://img/unknown.jpg"));

    MonthStatisticsResp february = result.get(1);
    assertThat(february.month()).isEqualTo(2);
    assertThat(february.name()).isEqualTo("February");
    assertThat(february.lineUp()).hasSize(1);
    assertThat(february.lineUp().getFirst().figurines())
        .extracting(FigurineByMonthResp::id, FigurineByMonthResp::name, FigurineByMonthResp::url)
        .containsExactly(tuple(36L, "Virgo Shaka", "https://img/virgo.jpg"));

    verify(repository).findAllByYear(2026);
  }

  @Test
  void retrieveStatisticsByYear_shouldReturnEmptyWhenRepositoryReturnsNoData() {
    when(repository.findAllByYear(2024)).thenReturn(List.of());

    List<MonthStatisticsResp> result = service.retrieveStatisticsByYear(2024);

    assertThat(result).isEmpty();
    verify(repository).findAllByYear(2024);
  }

  private Figurine createFigurine(Long id, String name, LineUp lineUp, String imageUrl) {
    Figurine figurine = new Figurine();
    figurine.setId(id);
    figurine.setLegacyName(name);
    figurine.setNormalizedName(name);
    figurine.setLineup(lineUp);
    figurine.setOfficialImages(imageUrl == null ? null : List.of(imageUrl));
    return figurine;
  }

  private LineUp createLineUp(Long id, String description) {
    LineUp lineUp = new LineUp();
    lineUp.setId(id);
    lineUp.setDescription(description);
    return lineUp;
  }

  private Series createSeries(Long id, String description) {
    Series series = new Series();
    series.setId(id);
    series.setDescription(description);
    return series;
  }

  private Group createGroup(Long id, String description) {
    Group group = new Group();
    group.setId(id);
    group.setDescription(description);
    return group;
  }

  private Anniversary createAnniversary(Long id, String description) {
    Anniversary anniversary = new Anniversary();
    anniversary.setId(id);
    anniversary.setDescription(description);
    return anniversary;
  }

  private FigurineDistributor createDistributor(LocalDate releaseDate) {
    FigurineDistributor distributor = new FigurineDistributor();
    distributor.setReleaseDate(releaseDate);
    return distributor;
  }
}
