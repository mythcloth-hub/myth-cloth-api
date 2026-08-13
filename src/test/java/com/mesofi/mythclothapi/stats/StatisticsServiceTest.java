package com.mesofi.mythclothapi.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.mesofi.mythclothapi.stats.dto.MonthStatisticsResp;

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
        // anniversary.setDescription(description);
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
