package com.mesofi.mythclothapi.stats;

import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;
import com.mesofi.mythclothapi.figurines.repository.projection.FigurineReleaseYearSummaryProjection;
import com.mesofi.mythclothapi.integration.fix.CurrencyConversionService;
import com.mesofi.mythclothapi.stats.dto.FigurineByMonthResp;
import com.mesofi.mythclothapi.stats.dto.FigurinePriceResp;
import com.mesofi.mythclothapi.stats.dto.LineUpByMonthResp;
import com.mesofi.mythclothapi.stats.dto.LineUpCountResp;
import com.mesofi.mythclothapi.stats.dto.MonthStatisticsResp;
import com.mesofi.mythclothapi.stats.dto.StatisticsResp;
import com.mesofi.mythclothapi.stats.dto.YearReleasePriceResp;
import com.mesofi.mythclothapi.stats.dto.YearStatisticsResp;
import com.mesofi.mythclothapi.stats.model.ReleasePrices;
import com.mesofi.mythclothapi.utils.CurrencyConverter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for generating aggregated figurine statistics used by the
 * stats endpoints.
 *
 * <p>
 * It provides:
 *
 * <ul>
 * <li>global counters by catalog and release status,
 * <li>yearly release totals grouped by line-up,
 * <li>monthly release breakdown for a specific year.
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    public static final String PRICING_SUMMARY_CACHE = "pricing-summary";

    private final FigurineRepository figurineRepository;
    private final LineUpRepository lineUpRepository;
    private final SeriesRepository seriesRepository;
    private final GroupRepository groupRepository;
    private final AnniversaryRepository anniversaryRepository;
    private final CurrencyConversionService currencyConversionService;
    private final StatisticsRepository statisticsRepository;

    /**
     * Retrieves a global statistics snapshot for figurines that match the given
     * filter.
     *
     * @param filter
     *            search filter used to constrain figurines included in the
     *            aggregation
     * @return aggregate totals by catalog and release status
     */
    public StatisticsResp retrieveStatistics(@NotNull FigurineFilter filter) {
        List<Figurine> allFigurines = figurineRepository.findAll(filter);

        return new StatisticsResp(allFigurines.size(),
                countByCatalog(allFigurines, lineUpRepository.findAll(), Figurine::getLineup, LineUp::getId,
                        LineUp::getDescription),
                countByCatalog(allFigurines, seriesRepository.findAll(), Figurine::getSeries, Series::getId,
                        Series::getDescription),
                countByCatalog(allFigurines, groupRepository.findAll(), Figurine::getGroup, Group::getId,
                        Group::getDescription),
                countByCatalog(allFigurines, anniversaryRepository.findAll(), Figurine::getAnniversary,
                        Anniversary::getId, Anniversary::getName),
                countByReleaseStatus(allFigurines));
    }

    /**
     * Retrieves yearly release statistics grouped by line-up description.
     *
     * <p>
     * Only figurines currently evaluated as {@code RELEASED} are considered. Each
     * figurine is counted against the release year of its first distributor record
     * when present.
     *
     * @return list of yearly aggregates sorted by year (ascending)
     */
    public List<YearStatisticsResp> retrieveStatisticsByReleases() {
        log.info("Retrieving yearly release statistics");

        List<FigurineReleaseYearSummaryProjection> summary = figurineRepository.getReleaseYearSummary();

        Map<Integer, Map<String, Integer>> map = new TreeMap<>();

        for (FigurineReleaseYearSummaryProjection summaryProjection : summary) {
            int year = summaryProjection.getReleaseYear();
            String lineUpDescription = summaryProjection.getLineupDescription();
            int count = summaryProjection.getFigurineCount().intValue();

            map.computeIfAbsent(year, k -> new HashMap<>()).put(lineUpDescription, count);
        }

        List<YearStatisticsResp> resp = new ArrayList<>();
        map.forEach((year, lineUpMap) -> {
            List<LineUpCountResp> lineUp = new ArrayList<>();
            lineUpMap.forEach((line, count) -> lineUp.add(new LineUpCountResp(line, count)));

            resp.add(new YearStatisticsResp(year, lineUp));
        });

        return resp;
    }

    /**
     * Retrieves monthly release statistics for a specific year.
     *
     * <p>
     * Results are grouped first by month and then by line-up, with figurines sorted
     * by normalized name inside each line-up.
     *
     * @param year
     *            year to inspect
     * @return month-based release breakdown for the requested year
     */
    public List<MonthStatisticsResp> retrieveStatisticsByYear(int year) {
        List<Figurine> respList = figurineRepository.findAllByYear(year);

        Map<Integer, Map<String, List<FigurineByMonthResp>>> groupedByMonthAndLineUp = new HashMap<>();

        respList.forEach(figurine -> {
            Optional<Integer> month = extractReleaseMonthForYear(figurine, year);
            if (month.isEmpty()) {
                return;
            }

            String lineUp = Optional.ofNullable(figurine.getLineup()).map(LineUp::getDescription).orElse("Unknown");

            groupedByMonthAndLineUp.computeIfAbsent(month.get(), key -> new HashMap<>())
                    .computeIfAbsent(lineUp, key -> new ArrayList<>())
                    .add(new FigurineByMonthResp(figurine.getId(), figurine.getNormalizedName(),
                            resolveFigurineUrl(figurine), figurine.getCurrentReleaseStatus()));
        });

        return groupedByMonthAndLineUp.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(monthEntry -> {
            int month = monthEntry.getKey();

            List<LineUpByMonthResp> lineUp = monthEntry.getValue().entrySet().stream()
                    .map(lineEntry -> new LineUpByMonthResp(lineEntry.getKey(), lineEntry.getValue().stream()
                            .sorted(Comparator.comparing(FigurineByMonthResp::name)).toList()))
                    .sorted(Comparator.comparing(LineUpByMonthResp::line)).toList();

            return new MonthStatisticsResp(month, Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    lineUp);
        }).toList();
    }

    /**
     * Retrieves yearly release-price aggregates for figurines that match the given
     * filter.
     *
     * <p>
     * Only figurines currently evaluated as {@code RELEASED} are considered. Prices
     * are normalized to JPY before calculating yearly average, highest, and lowest
     * values.
     *
     * @param currency
     *            currency to which prices should be converted
     * @return list of yearly price summaries sorted by year (ascending)
     */
    @Cacheable(value = PRICING_SUMMARY_CACHE, key = "T(java.util.Objects).hash(#currency)")
    public List<YearReleasePriceResp> retrieveYearlyReleasePrices(@Nonnull Currency currency) {
        log.info("Retrieving yearly release-price statistics with currency: {}", currency);

        Map<Integer, ReleasePrices> releasePricesByYearMap = new TreeMap<>();

        List<StatisticsReleasedFigurineProjection> releasedFigurines = statisticsRepository
                .findReleasedFigurineStatistics();

        for (StatisticsReleasedFigurineProjection projection : releasedFigurines) {
            int year = projection.getReleaseDate().getYear();
            BigDecimal price = projection.getPrice();
            if (price == null) {
                log.warn("Price is null for figurine id: {}", projection.getId());
                continue;
            }

            if (releasePricesByYearMap.containsKey(year)) {
                ReleasePrices relPrices = releasePricesByYearMap.get(year);

                relPrices.setHighest(
                        BigDecimal.valueOf(Double.max(price.doubleValue(), relPrices.getHighest().doubleValue())));
                relPrices.setLowest(
                        BigDecimal.valueOf(Double.min(price.doubleValue(), relPrices.getLowest().doubleValue())));

                if (price.doubleValue() >= relPrices.getHighest().doubleValue()) {
                    relPrices.setHighestPriceFigurineId(projection.getId());
                    relPrices.setHighestPriceFigurineName(projection.getName());
                }
                if (price.doubleValue() <= relPrices.getLowest().doubleValue()) {
                    relPrices.setLowestPriceFigurineId(projection.getId());
                    relPrices.setLowestPriceFigurineName(projection.getName());
                }

                relPrices.setCount(relPrices.getCount() + 1);
                relPrices.setTotal(relPrices.getTotal().add(price));
                relPrices.setAverage(
                        relPrices.getTotal().divide(BigDecimal.valueOf(relPrices.getCount()), RoundingMode.HALF_UP));

            } else {
                ReleasePrices releasePrices = ReleasePrices.builder().average(price).highest(price).lowest(price)
                        .highestPriceFigurineId(projection.getId()).highestPriceFigurineName(projection.getName())
                        .lowestPriceFigurineId(projection.getId()).lowestPriceFigurineName(projection.getName())
                        .total(price).count(1).build();

                releasePricesByYearMap.put(year, releasePrices);
            }
        }

        List<YearReleasePriceResp> respList = new ArrayList<>();

        releasePricesByYearMap.forEach((year, prices) -> {

            YearReleasePriceResp resp = new YearReleasePriceResp(year, getPrice(prices.getAverage(), currency),
                    getPrice(prices.getHighest(), currency), getPrice(prices.getLowest(), currency),
                    new FigurinePriceResp(prices.getHighestPriceFigurineId(), prices.getHighestPriceFigurineName(),
                            getImageUrlForFigurine(prices.getHighestPriceFigurineId())),
                    new FigurinePriceResp(prices.getLowestPriceFigurineId(), prices.getLowestPriceFigurineName(),
                            getImageUrlForFigurine(prices.getLowestPriceFigurineId())),
                    prices.getCount());

            respList.add(resp);
        });
        return respList;
    }

    /**
     * Retrieves the first official image URL for a figurine, if available.
     *
     * @param figurineId
     *            the ID of the figurine
     * @return the first official image URL, or an empty string if no images are
     *         found
     */
    private String getImageUrlForFigurine(Long figurineId) {
        List<String> officialImages = statisticsRepository.findOfficialImagesStatistics(figurineId);
        if (officialImages != null && !officialImages.isEmpty()) {
            return officialImages.getFirst();
        }
        return "";
    }

    /**
     * Converts the given price to the specified currency using the
     * {@link CurrencyConversionService}.
     *
     * @param price
     *            the price in JPY to convert
     * @param currency
     *            the target currency for conversion
     * @return the converted price in the specified currency, or the original price
     *         if the target currency is JPY
     */
    private BigDecimal getPrice(BigDecimal price, Currency currency) {
        if (CurrencyConverter.isDefaultCurrency(currency)) {
            return price;
        }
        return currencyConversionService.convert(price, JPY.toString(), currency.toString());
    }

    /** Counts figurines by calculated release status name. */
    private Map<String, Integer> countByReleaseStatus(List<Figurine> allFigurines) {
        return allFigurines.stream().map(Figurine::getCurrentReleaseStatus)
                .collect(Collectors.groupingBy(ReleaseStatus::name, Collectors.summingInt(status -> 1)));
    }

    /** Counts figurines per catalog entry using catalog id as the grouping key. */
    private <T> Map<String, Integer> countByCatalog(List<Figurine> allFigurines, List<T> allCatalogs,
            Function<Figurine, T> figurineCatalogSelector, Function<T, Long> catalogIdSelector,
            Function<T, String> catalogDescriptionSelector) {

        Map<Long, Long> figurinesByCatalogId = allFigurines.stream().map(figurineCatalogSelector)
                .filter(Objects::nonNull).collect(Collectors.groupingBy(catalogIdSelector, Collectors.counting()));

        Map<String, Integer> countByCatalog = new HashMap<>();
        allCatalogs.forEach(catalog -> {
            long count = figurinesByCatalogId.getOrDefault(catalogIdSelector.apply(catalog), 0L);
            countByCatalog.put(catalogDescriptionSelector.apply(catalog), (int) count);
        });

        return countByCatalog;
    }

    /** Extracts the earliest release month for a figurine in the provided year. */
    private Optional<Integer> extractReleaseMonthForYear(Figurine figurine, Integer year) {
        return figurine.getDistributors().stream().map(FigurineDistributor::getReleaseDate).filter(Objects::nonNull)
                .filter(releaseDate -> releaseDate.getYear() == year).map(LocalDate::getMonthValue)
                .min(Comparator.naturalOrder());
    }

    /** Resolves the first official figurine image URL when available. */
    private String resolveFigurineUrl(Figurine figurine) {
        if (figurine.getOfficialImages() != null && !figurine.getOfficialImages().isEmpty()) {
            return figurine.getOfficialImages().getFirst();
        }
        return "";
    }
}
