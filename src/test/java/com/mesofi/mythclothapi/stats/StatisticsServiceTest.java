package com.mesofi.mythclothapi.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.FigurineService;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;
import com.mesofi.mythclothapi.integration.fix.CurrencyConversionService;
import com.mesofi.mythclothapi.stats.dto.FigurineByMonthResp;
import com.mesofi.mythclothapi.stats.dto.MonthStatisticsResp;
import com.mesofi.mythclothapi.stats.dto.YearReleasePriceResp;
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
  @Mock private CurrencyConversionService currencyConversionService;

  @Test
  void retrieveStatistics_shouldAggregateCatalogsAndStatuses() {
    LineUp ex = lineUp(1L, "Myth Cloth EX");
    LineUp myth = lineUp(2L, "Myth Cloth");
    Series hades = series(11L, "Hades");
    Series sanctuary = series(12L, "Sanctuary");
    Group gold = group(21L, "Gold");
    Group bronze = group(22L, "Bronze");
    Anniversary ann10 = anniversary(31L, "10th");
    Anniversary ann20 = anniversary(32L, "20th");

    Figurine shiryu = figurine(1L, "Shiryu", ex, "https://img/shiryu.jpg");
    shiryu.setSeries(hades);
    shiryu.setGroup(gold);
    shiryu.setAnniversary(ann10);

    Figurine ikki = figurine(2L, "Ikki", ex, "https://img/ikki.jpg");
    ikki.setSeries(sanctuary);
    ikki.setGroup(bronze);
    ikki.setAnniversary(ann20);

    Figurine hyoga = figurine(3L, "Hyoga", myth, "https://img/hyoga.jpg");
    hyoga.setSeries(hades);
    hyoga.setGroup(gold);
    hyoga.setAnniversary(ann10);

    Figurine skippedCatalog = figurine(4L, "No Catalog", null, "https://img/no.jpg");

    when(repository.findAll(EMPTY_FILTER)).thenReturn(List.of(shiryu, ikki, hyoga, skippedCatalog));
    when(lineUpRepository.findAll()).thenReturn(List.of(ex, myth));
    when(seriesRepository.findAll()).thenReturn(List.of(hades, sanctuary));
    when(groupRepository.findAll()).thenReturn(List.of(gold, bronze));
    when(anniversaryRepository.findAll()).thenReturn(List.of(ann10, ann20));

    when(figurineService.calculateReleaseStatus(shiryu)).thenReturn(ReleaseStatus.RELEASED);
    when(figurineService.calculateReleaseStatus(ikki)).thenReturn(ReleaseStatus.ANNOUNCED);
    when(figurineService.calculateReleaseStatus(hyoga)).thenReturn(ReleaseStatus.RELEASED);
    when(figurineService.calculateReleaseStatus(skippedCatalog))
        .thenReturn(ReleaseStatus.PROTOTYPE);

    var result = service.retrieveStatistics(EMPTY_FILTER);

    assertThat(result.totalFigurines()).isEqualTo(4);
    assertThat(result.countByLineUp())
        .containsExactlyInAnyOrderEntriesOf(Map.of("Myth Cloth EX", 2, "Myth Cloth", 1));
    assertThat(result.countBySeries())
        .containsExactlyInAnyOrderEntriesOf(Map.of("Hades", 2, "Sanctuary", 1));
    assertThat(result.countByGroup())
        .containsExactlyInAnyOrderEntriesOf(Map.of("Gold", 2, "Bronze", 1));
    assertThat(result.countByAnniversary())
        .containsExactlyInAnyOrderEntriesOf(Map.of("10th", 2, "20th", 1));
    assertThat(result.totalByReleaseStatus())
        .containsExactlyInAnyOrderEntriesOf(Map.of("RELEASED", 2, "ANNOUNCED", 1, "PROTOTYPE", 1));

    verify(repository).findAll(EMPTY_FILTER);
  }

  @Test
  void retrieveStatisticsByReleases_shouldCountReleasedByFirstDistributorYearAndLineup() {
    LineUp ex = lineUp(1L, "Myth Cloth EX");
    LineUp myth = lineUp(2L, "Myth Cloth");

    Figurine saga = figurine(10L, "Saga", ex, "https://img/saga.jpg");
    saga.setDistributors(
        List.of(distributor(LocalDate.of(2024, 4, 1)), distributor(LocalDate.of(2025, 1, 1))));

    Figurine mu = figurine(11L, "Mu", ex, "https://img/mu.jpg");
    mu.setDistributors(List.of(distributor(LocalDate.of(2024, 6, 1))));

    Figurine aiolos = figurine(12L, "Aiolos", myth, "https://img/aiolos.jpg");
    aiolos.setDistributors(List.of(distributor(LocalDate.of(2024, 2, 1))));

    Figurine announced = figurine(13L, "Announced", ex, "https://img/a.jpg");
    announced.setDistributors(List.of(distributor(LocalDate.of(2024, 3, 1))));

    Figurine noDistributor = figurine(14L, "No Distributor", myth, "https://img/nd.jpg");
    noDistributor.setDistributors(List.of());

    when(repository.findAll(EMPTY_FILTER))
        .thenReturn(List.of(saga, mu, aiolos, announced, noDistributor));
    when(figurineService.calculateReleaseStatus(saga)).thenReturn(ReleaseStatus.RELEASED);
    when(figurineService.calculateReleaseStatus(mu)).thenReturn(ReleaseStatus.RELEASED);
    when(figurineService.calculateReleaseStatus(aiolos)).thenReturn(ReleaseStatus.RELEASED);
    when(figurineService.calculateReleaseStatus(announced)).thenReturn(ReleaseStatus.ANNOUNCED);
    when(figurineService.calculateReleaseStatus(noDistributor)).thenReturn(ReleaseStatus.RELEASED);

    List<YearStatisticsResp> result = service.retrieveStatisticsByReleases(EMPTY_FILTER);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().year()).isEqualTo(2024);
    assertThat(result.getFirst().lineUp())
        .extracting("line", "count")
        .containsExactlyInAnyOrder(tuple("Myth Cloth EX", 2), tuple("Myth Cloth", 1));

    verify(repository).findAll(EMPTY_FILTER);
  }

  @Test
  void retrieveStatisticsByYear_shouldGroupByMonthAndLineupAndApplyFallbacks() {
    LineUp bronze = lineUp(1L, "Bronze");
    LineUp gold = lineUp(2L, "Gold");

    Figurine shun = figurine(42L, "Andromeda Shun", bronze, null);
    shun.setOfficialImages(List.of());
    shun.setDistributors(
        List.of(distributor(LocalDate.of(2026, 1, 10)), distributor(LocalDate.of(2026, 2, 10))));

    Figurine ikki = figurine(44L, "Phoenix Ikki", bronze, "https://img/phoenix.jpg");
    ikki.setDistributors(List.of(distributor(LocalDate.of(2026, 1, 22))));

    Figurine shaka = figurine(36L, "Virgo Shaka", gold, "https://img/virgo.jpg");
    shaka.setDistributors(List.of(distributor(LocalDate.of(2026, 2, 2))));

    Figurine unknown = figurine(50L, "Unknown Fighter", null, "https://img/unknown.jpg");
    unknown.setDistributors(List.of(distributor(LocalDate.of(2026, 1, 15))));

    Figurine skipped = figurine(51L, "Skipped", gold, "https://img/skipped.jpg");
    skipped.setDistributors(List.of(distributor(LocalDate.of(2025, 12, 12))));

    when(repository.findAllByYear(2026)).thenReturn(List.of(ikki, shaka, shun, unknown, skipped));

    List<MonthStatisticsResp> result = service.retrieveStatisticsByYear(2026);

    assertThat(result).hasSize(2);
    assertThat(result).extracting(MonthStatisticsResp::month).containsExactly(1, 2);

    MonthStatisticsResp january = result.getFirst();
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
    assertThat(february.name()).isEqualTo("February");
    assertThat(february.lineUp()).hasSize(1);
    assertThat(february.lineUp().getFirst().figurines())
        .extracting(FigurineByMonthResp::id, FigurineByMonthResp::name, FigurineByMonthResp::url)
        .containsExactly(tuple(36L, "Virgo Shaka", "https://img/virgo.jpg"));

    verify(repository).findAllByYear(2026);
  }

  @Test
  void retrieveStatisticsByYear_shouldReturnEmptyWhenNoData() {
    when(repository.findAllByYear(2024)).thenReturn(List.of());

    List<MonthStatisticsResp> result = service.retrieveStatisticsByYear(2024);

    assertThat(result).isEmpty();
    verify(repository).findAllByYear(2024);
  }

  @Test
  void retrieveYearlyReleasePrices_shouldAggregateAndConvertToJpy() {
    LineUp ex = lineUp(1L, "Myth Cloth EX");

    Figurine jpyFigurine = figurine(100L, "Aldebaran", ex, "https://img/aldebaran.jpg");
    jpyFigurine.setDistributors(
        List.of(distributor(LocalDate.of(2025, 1, 10), 11000.0, CurrencyCode.JPY)));

    Figurine usdFigurine = figurine(101L, "Dohko", ex, "https://img/dohko.jpg");
    usdFigurine.setDistributors(
        List.of(distributor(LocalDate.of(2025, 2, 15), 100.0, CurrencyCode.USD)));

    when(repository.findAll(EMPTY_FILTER)).thenReturn(List.of(jpyFigurine, usdFigurine));
    when(figurineService.calculateReleaseStatus(jpyFigurine)).thenReturn(ReleaseStatus.RELEASED);
    when(figurineService.calculateReleaseStatus(usdFigurine)).thenReturn(ReleaseStatus.RELEASED);
    when(currencyConversionService.convert(any(BigDecimal.class), eq("USD"), eq("JPY")))
        .thenReturn(new BigDecimal("15000.0"));

    List<YearReleasePriceResp> result = service.retrieveYearlyReleasePrices(EMPTY_FILTER);

    assertThat(result).hasSize(1);
    YearReleasePriceResp yearly = result.getFirst();
    assertThat(yearly.year()).isEqualTo(2025);
    assertThat(yearly.averageReleasePrice()).isEqualByComparingTo("13000.00");
    assertThat(yearly.highestReleasePrice()).isEqualByComparingTo("15000.00");
    assertThat(yearly.lowestReleasePrice()).isEqualByComparingTo("11000.00");
    assertThat(yearly.releaseCount()).isEqualTo(2);
    assertThat(yearly.highestPriceFigurines().id()).isEqualTo(101L);
    assertThat(yearly.lowestPriceFigurines().id()).isEqualTo(100L);

    verify(currencyConversionService).convert(any(BigDecimal.class), eq("USD"), eq("JPY"));
  }

  @Test
  void retrieveYearlyReleasePrices_shouldSkipNullPricesAndIgnoreNonReleased() {
    LineUp ex = lineUp(1L, "Myth Cloth EX");

    Figurine nullPrice = figurine(200L, "Shaka", ex, "https://img/shaka.jpg");
    nullPrice.setDistributors(
        List.of(distributor(LocalDate.of(2026, 4, 1), null, CurrencyCode.JPY)));

    Figurine announced = figurine(201L, "Milo", ex, "https://img/milo.jpg");
    announced.setDistributors(
        List.of(distributor(LocalDate.of(2026, 4, 1), 12000.0, CurrencyCode.JPY)));

    when(repository.findAll(EMPTY_FILTER)).thenReturn(List.of(nullPrice, announced));
    when(figurineService.calculateReleaseStatus(nullPrice)).thenReturn(ReleaseStatus.RELEASED);
    when(figurineService.calculateReleaseStatus(announced)).thenReturn(ReleaseStatus.ANNOUNCED);

    List<YearReleasePriceResp> result = service.retrieveYearlyReleasePrices(EMPTY_FILTER);

    assertThat(result).isEmpty();
    verify(currencyConversionService, never()).convert(any(BigDecimal.class), eq("JPY"), eq("JPY"));
  }

  @Test
  void retrieveYearlyReleasePrices_shouldUpdateLowestPriceAndLowestFigurineOnLowerAndEqualValues() {
    LineUp ex = lineUp(1L, "Myth Cloth EX");

    Figurine high = figurine(300L, "Aiolia", ex, "https://img/aiolia.jpg");
    high.setDistributors(List.of(distributor(LocalDate.of(2025, 1, 1), 15000.0, CurrencyCode.JPY)));

    Figurine lower = figurine(301L, "Milo", ex, "https://img/milo.jpg");
    lower.setDistributors(List.of(distributor(LocalDate.of(2025, 2, 1), 100.0, CurrencyCode.USD)));

    Figurine equalLowest = figurine(302L, "Aphrodite", ex, "https://img/aphrodite.jpg");
    equalLowest.setDistributors(
        List.of(distributor(LocalDate.of(2025, 3, 1), 10000.0, CurrencyCode.JPY)));

    when(repository.findAll(EMPTY_FILTER)).thenReturn(List.of(high, lower, equalLowest));
    when(figurineService.calculateReleaseStatus(high)).thenReturn(ReleaseStatus.RELEASED);
    when(figurineService.calculateReleaseStatus(lower)).thenReturn(ReleaseStatus.RELEASED);
    when(figurineService.calculateReleaseStatus(equalLowest)).thenReturn(ReleaseStatus.RELEASED);
    when(currencyConversionService.convert(any(BigDecimal.class), eq("USD"), eq("JPY")))
        .thenReturn(new BigDecimal("10000.0"));

    List<YearReleasePriceResp> result = service.retrieveYearlyReleasePrices(EMPTY_FILTER);

    assertThat(result).hasSize(1);
    YearReleasePriceResp yearly = result.getFirst();
    assertThat(yearly.year()).isEqualTo(2025);
    assertThat(yearly.averageReleasePrice()).isEqualByComparingTo("11666.67");
    assertThat(yearly.highestReleasePrice()).isEqualByComparingTo("15000.00");
    assertThat(yearly.lowestReleasePrice()).isEqualByComparingTo("10000.00");
    assertThat(yearly.releaseCount()).isEqualTo(3);
    assertThat(yearly.highestPriceFigurines().id()).isEqualTo(300L);
    assertThat(yearly.lowestPriceFigurines().id()).isEqualTo(302L);

    verify(currencyConversionService).convert(any(BigDecimal.class), eq("USD"), eq("JPY"));
  }

  private Figurine figurine(Long id, String name, LineUp lineUp, String imageUrl) {
    Figurine figurine = new Figurine();
    figurine.setId(id);
    figurine.setLegacyName(name);
    figurine.setNormalizedName(name);
    figurine.setLineup(lineUp);
    figurine.setOfficialImages(imageUrl == null ? null : List.of(imageUrl));
    return figurine;
  }

  private LineUp lineUp(Long id, String description) {
    LineUp lineUp = new LineUp();
    lineUp.setId(id);
    lineUp.setDescription(description);
    return lineUp;
  }

  private Series series(Long id, String description) {
    Series series = new Series();
    series.setId(id);
    series.setDescription(description);
    return series;
  }

  private Group group(Long id, String description) {
    Group group = new Group();
    group.setId(id);
    group.setDescription(description);
    return group;
  }

  private Anniversary anniversary(Long id, String description) {
    Anniversary anniversary = new Anniversary();
    anniversary.setId(id);
    anniversary.setDescription(description);
    return anniversary;
  }

  private FigurineDistributor distributor(LocalDate releaseDate) {
    FigurineDistributor distributor = new FigurineDistributor();
    distributor.setReleaseDate(releaseDate);
    return distributor;
  }

  private FigurineDistributor distributor(
      LocalDate releaseDate, Double price, CurrencyCode currency) {
    FigurineDistributor distributor = distributor(releaseDate);
    distributor.setPrice(price);
    distributor.setCurrency(currency);
    return distributor;
  }
}
