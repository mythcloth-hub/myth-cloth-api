package com.mesofi.mythclothapi.stats;

import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.ANNOUNCED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.RELEASED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;
import com.mesofi.mythclothapi.integration.fix.CurrencyConversionService;
import com.mesofi.mythclothapi.stats.dto.FigurineByMonthResp;
import com.mesofi.mythclothapi.stats.dto.LineUpCountResp;
import com.mesofi.mythclothapi.stats.dto.MonthStatisticsResp;
import com.mesofi.mythclothapi.stats.dto.StatisticsResp;
import com.mesofi.mythclothapi.stats.dto.YearReleasePriceResp;
import com.mesofi.mythclothapi.stats.dto.YearStatisticsResp;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    private static final FigurineFilter EMPTY_FILTER = new FigurineFilter(null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null);

    @InjectMocks
    private StatisticsService service;

    @Mock
    private FigurineService figurineService;
    @Mock
    private FigurineRepository repository;
    @Mock
    private LineUpRepository lineUpRepository;
    @Mock
    private SeriesRepository seriesRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private AnniversaryRepository anniversaryRepository;
    @Mock
    private CurrencyConversionService currencyConversionService;

    @Test
    void retrieveStatisticsByYear_shouldGroupByMonthAndLineupAndApplyFallbacks() {
        LineUp bronze = lineUp(1L, "Bronze");
        LineUp gold = lineUp(2L, "Gold");

        Figurine shun = figurine(42L, "Andromeda Shun", bronze, null);
        shun.setOfficialImages(List.of());
        shun.setDistributors(List.of(distributor(LocalDate.of(2026, 1, 10)), distributor(LocalDate.of(2026, 2, 10))));

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
                .containsExactly(tuple(42L, "Andromeda Shun", ""),
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
    void retrieveStatistics_shouldAggregateCatalogsAndReleaseStatusTotals() {
        LineUp bronze = lineUp(1L, "Bronze");
        LineUp gold = lineUp(2L, "Gold");
        Series series = series(10L, "Classic");
        Group group = group(20L, "Saints");
        Anniversary anniversary = anniversary(30L, "Saints Day");

        Figurine shun = figurine(42L, "Andromeda Shun", bronze, null);
        shun.setSeries(series);
        shun.setGroup(group);
        shun.setAnniversary(anniversary);
        shun.setCurrentReleaseStatus(RELEASED);

        Figurine ikki = figurine(44L, "Phoenix Ikki", bronze, "https://img/ikki.jpg");
        ikki.setSeries(null);
        ikki.setGroup(group);
        ikki.setCurrentReleaseStatus(ANNOUNCED);

        Figurine shaka = figurine(36L, "Virgo Shaka", gold, null);
        shaka.setSeries(series);
        shaka.setGroup(null);
        shaka.setAnniversary(anniversary);
        shaka.setCurrentReleaseStatus(RELEASED);

        when(repository.findAll(EMPTY_FILTER)).thenReturn(List.of(shun, ikki, shaka));
        when(lineUpRepository.findAll()).thenReturn(List.of(bronze, gold));
        when(seriesRepository.findAll()).thenReturn(List.of(series));
        when(groupRepository.findAll()).thenReturn(List.of(group));
        when(anniversaryRepository.findAll()).thenReturn(List.of(anniversary));

        StatisticsResp result = service.retrieveStatistics(EMPTY_FILTER);

        assertThat(result.totalFigurines()).isEqualTo(3);
        assertThat(result.countByLineUp()).containsEntry("Bronze", 2).containsEntry("Gold", 1);
        assertThat(result.countBySeries()).containsEntry("Classic", 2);
        assertThat(result.countByGroup()).containsEntry("Saints", 2);
        assertThat(result.countByAnniversary()).containsEntry("Saints Day", 2);
        assertThat(result.totalByReleaseStatus()).containsEntry("RELEASED", 2).containsEntry("ANNOUNCED", 1);
    }

    @Test
    void retrieveStatisticsByReleases_shouldOnlyIncludeReleasedFigurinesAndUseFirstDistributorYear() {
        LineUp bronze = lineUp(1L, "Bronze");
        LineUp gold = lineUp(2L, "Gold");

        Figurine shun = figurine(42L, "Andromeda Shun", bronze, null);
        shun.setCurrentReleaseStatus(RELEASED);
        shun.setDistributors(List.of(distributor(LocalDate.of(2025, 11, 10)), distributor(LocalDate.of(2026, 1, 10))));

        Figurine ikki = figurine(44L, "Phoenix Ikki", bronze, null);
        ikki.setCurrentReleaseStatus(RELEASED);
        ikki.setDistributors(List.of(distributor(LocalDate.of(2026, 4, 22))));

        Figurine shaka = figurine(36L, "Virgo Shaka", gold, null);
        shaka.setCurrentReleaseStatus(RELEASED);
        shaka.setDistributors(List.of(distributor(LocalDate.of(2026, 2, 2))));

        Figurine anotherGold = figurine(37L, "Leo Aiolia", gold, null);
        anotherGold.setCurrentReleaseStatus(RELEASED);
        anotherGold.setDistributors(List.of(distributor(LocalDate.of(2026, 3, 7))));

        Figurine announced = figurine(38L, "Gemini Saga", gold, null);
        announced.setCurrentReleaseStatus(ANNOUNCED);
        announced.setDistributors(List.of(distributor(LocalDate.of(2026, 6, 1))));

        Figurine missing = figurine(50L, "Unknown Fighter", gold, null);
        missing.setCurrentReleaseStatus(RELEASED);
        missing.setDistributors(List.of());

        when(repository.getReleaseYearSummary()).thenReturn(
                List.of(projection(2025, "Bronze", 1L), projection(2026, "Bronze", 1L), projection(2026, "Gold", 2L)));

        List<YearStatisticsResp> result = service.retrieveStatisticsByReleases();

        assertThat(result).extracting(YearStatisticsResp::year).containsExactly(2025, 2026);
        assertThat(result.getFirst().lineUp()).extracting(LineUpCountResp::line, LineUpCountResp::count)
                .containsExactly(tuple("Bronze", 1));
        assertThat(result.get(1).lineUp()).extracting(LineUpCountResp::line, LineUpCountResp::count)
                .containsExactlyInAnyOrder(tuple("Bronze", 1), tuple("Gold", 2));
    }

    @Test
    void retrieveYearlyReleasePrices_shouldNormalizePricesAndKeepHighestLowestByEquality() {
        LineUp bronze = lineUp(1L, "Bronze");

        Figurine primaryHigh = figurine(101L, "Primary High", bronze, "https://img/high-1.jpg");
        primaryHigh.setCurrentReleaseStatus(RELEASED);
        primaryHigh.setDistributors(List.of(distributor(LocalDate.of(2024, 2, 10), 2000.0, CurrencyCode.USD)));

        Figurine secondaryHigh = figurine(102L, "Secondary High", bronze, "https://img/high-2.jpg");
        secondaryHigh.setCurrentReleaseStatus(RELEASED);
        secondaryHigh.setDistributors(List.of(distributor(LocalDate.of(2024, 3, 10), 3000.0, CurrencyCode.JPY)));

        Figurine tiedHigh = figurine(103L, "Tied High", bronze, "https://img/tied-high.jpg");
        tiedHigh.setCurrentReleaseStatus(RELEASED);
        tiedHigh.setDistributors(List.of(distributor(LocalDate.of(2024, 4, 10), 3000.0, CurrencyCode.JPY)));

        Figurine cheap = figurine(104L, "Cheap", bronze, "https://img/cheap.jpg");
        cheap.setCurrentReleaseStatus(RELEASED);
        cheap.setDistributors(List.of(distributor(LocalDate.of(2024, 5, 10), 1000.0, CurrencyCode.JPY)));

        Figurine missingPrice = figurine(105L, "No Price", bronze, "https://img/no-price.jpg");
        missingPrice.setCurrentReleaseStatus(RELEASED);
        missingPrice.setDistributors(List.of(distributor(LocalDate.of(2024, 6, 10), null, null)));

        Figurine announced = figurine(106L, "Announced", bronze, "https://img/announced.jpg");
        announced.setCurrentReleaseStatus(ANNOUNCED);
        announced.setDistributors(List.of(distributor(LocalDate.of(2025, 1, 1), 5000.0, CurrencyCode.JPY)));

        when(repository.findAll(EMPTY_FILTER))
                .thenReturn(List.of(primaryHigh, secondaryHigh, tiedHigh, cheap, missingPrice, announced));
        when(currencyConversionService.convert(any(BigDecimal.class), eq("USD"), eq("JPY")))
                .thenReturn(new BigDecimal("2400.00"));

        List<YearReleasePriceResp> result = service.retrieveYearlyReleasePrices(EMPTY_FILTER);

        assertThat(result).extracting(YearReleasePriceResp::year).containsExactly(2024);
        YearReleasePriceResp year2024 = result.getFirst();
        assertThat(year2024.averageReleasePrice()).isEqualByComparingTo("2350.00");
        assertThat(year2024.highestReleasePrice()).isEqualByComparingTo("3000.00");
        assertThat(year2024.lowestReleasePrice()).isEqualByComparingTo("1000.00");
        assertThat(year2024.highestPriceFigurines().name()).isEqualTo("Tied High");
        assertThat(year2024.lowestPriceFigurines().name()).isEqualTo("Cheap");
        assertThat(year2024.releaseCount()).isEqualTo(4);
    }

    private com.mesofi.mythclothapi.figurines.repository.projection.FigurineReleaseYearSummaryProjection projection(
            int releaseYear, String lineupDescription, Long figurineCount) {
        return new com.mesofi.mythclothapi.figurines.repository.projection.FigurineReleaseYearSummaryProjection() {
            @Override
            public Integer getReleaseYear() {
                return releaseYear;
            }

            @Override
            public String getLineupDescription() {
                return lineupDescription;
            }

            @Override
            public Long getFigurineCount() {
                return figurineCount;
            }
        };
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
        anniversary.setName(description);
        return anniversary;
    }

    private FigurineDistributor distributor(LocalDate releaseDate) {
        FigurineDistributor distributor = new FigurineDistributor();
        distributor.setReleaseDate(releaseDate);
        return distributor;
    }

    private FigurineDistributor distributor(LocalDate releaseDate, Double price, CurrencyCode currency) {
        FigurineDistributor distributor = distributor(releaseDate);
        distributor.setPrice(price);
        distributor.setCurrency(currency);
        return distributor;
    }
}
