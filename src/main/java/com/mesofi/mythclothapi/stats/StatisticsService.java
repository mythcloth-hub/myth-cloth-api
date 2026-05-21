package com.mesofi.mythclothapi.stats;

import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.RELEASED;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotNull;

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
import com.mesofi.mythclothapi.figurines.FigurineService;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;
import com.mesofi.mythclothapi.stats.dto.FigurineByMonthResp;
import com.mesofi.mythclothapi.stats.dto.LineUpByMonthResp;
import com.mesofi.mythclothapi.stats.dto.LineUpCountResp;
import com.mesofi.mythclothapi.stats.dto.MonthStatisticsResp;
import com.mesofi.mythclothapi.stats.dto.StatisticsResp;
import com.mesofi.mythclothapi.stats.dto.YearStatisticsResp;

import lombok.RequiredArgsConstructor;

/**
 * Provides aggregated statistics for figurines across catalogs and release timelines.
 *
 * <p>The service exposes entry points for overall totals, yearly release summaries and monthly
 * release details.
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

  private final FigurineService figurineService;
  private final FigurineRepository repository;
  private final LineUpRepository lineUpRepository;
  private final SeriesRepository seriesRepository;
  private final GroupRepository groupRepository;
  private final AnniversaryRepository anniversaryRepository;

  /**
   * Builds a global statistics snapshot for the figurines that match the provided filter.
   *
   * @param filter filters applied to the figurine search
   * @return aggregate totals by catalog and release status
   */
  public StatisticsResp retrieveStatistics(@NotNull FigurineFilter filter) {
    List<Figurine> allFigurines = repository.findAll(filter);

    return new StatisticsResp(
        allFigurines.size(),
        countByCatalog(
            allFigurines,
            lineUpRepository.findAll(),
            Figurine::getLineup,
            LineUp::getId,
            LineUp::getDescription),
        countByCatalog(
            allFigurines,
            seriesRepository.findAll(),
            Figurine::getSeries,
            Series::getId,
            Series::getDescription),
        countByCatalog(
            allFigurines,
            groupRepository.findAll(),
            Figurine::getGroup,
            Group::getId,
            Group::getDescription),
        countByCatalog(
            allFigurines,
            anniversaryRepository.findAll(),
            Figurine::getAnniversary,
            Anniversary::getId,
            Anniversary::getDescription),
        countByReleaseStatus(allFigurines));
  }

  /**
   * Returns yearly release totals per line-up.
   *
   * <p>Only figurines currently considered {@code RELEASED} or {@code ANNOUNCED} are counted. A
   * figurine contributes at most once per year even if it has multiple distributor rows in the same
   * year.
   *
   * @param filter filters applied to the figurine search
   * @return ordered list of yearly totals from 2003 to next year
   */
  public List<YearStatisticsResp> retrieveStatisticsByReleases(@NotNull FigurineFilter filter) {
    List<Figurine> allFigurines = repository.findAll(filter);
    final int startingYear = 2003;
    final int endingYear = LocalDate.now().getYear() + 1;
    List<LineUp> allLineUps = lineUpRepository.findAll();

    Map<Long, String> lineUpDescById =
        allLineUps.stream().collect(Collectors.toMap(LineUp::getId, LineUp::getDescription));

    Map<Integer, Map<Long, Integer>> countByYearAndLineUp = new HashMap<>();

    allFigurines.stream()
        .filter(this::isReleased)
        .forEach(
            figurine -> {
              Long lineUpId = figurine.getLineup().getId();

              figurine.getDistributors().stream()
                  .map(FigurineDistributor::getReleaseDate)
                  .filter(Objects::nonNull)
                  .map(LocalDate::getYear)
                  .filter(year -> year >= startingYear && year <= endingYear)
                  .distinct()
                  .forEach(
                      year ->
                          countByYearAndLineUp
                              .computeIfAbsent(year, y -> new HashMap<>())
                              .merge(lineUpId, 1, Integer::sum));
            });

    List<YearStatisticsResp> respList = new ArrayList<>();
    for (int currYear = startingYear; currYear <= endingYear; currYear++) {
      Map<Long, Integer> yearlyByLineUp = countByYearAndLineUp.getOrDefault(currYear, Map.of());
      List<LineUpCountResp> countByLineUp =
          yearlyByLineUp.entrySet().stream()
              .filter(entry -> entry.getValue() > 0)
              .map(
                  entry ->
                      new LineUpCountResp(
                          lineUpDescById.getOrDefault(entry.getKey(), "Unknown"), entry.getValue()))
              .sorted(Comparator.comparing(LineUpCountResp::line))
              .toList();

      respList.add(new YearStatisticsResp(currYear, countByLineUp));
    }

    return respList;
  }

  /**
   * Returns monthly releases for a specific year, grouped by line-up.
   *
   * <p>For each figurine, the earliest release month found in the requested year is used.
   *
   * @param year target year
   * @return months sorted in ascending order with line-ups and figurines sorted alphabetically
   */
  public List<MonthStatisticsResp> retrieveStatisticsByYear(Integer year) {
    List<Figurine> respList = repository.findAllByYear(year);

    Map<Integer, Map<String, List<FigurineByMonthResp>>> groupedByMonthAndLineUp = new HashMap<>();

    respList.forEach(
        figurine -> {
          Optional<Integer> month = extractReleaseMonthForYear(figurine, year);
          if (month.isEmpty()) {
            return;
          }

          String lineUp =
              Optional.ofNullable(figurine.getLineup())
                  .map(LineUp::getDescription)
                  .orElse("Unknown");

          groupedByMonthAndLineUp
              .computeIfAbsent(month.get(), key -> new HashMap<>())
              .computeIfAbsent(lineUp, key -> new ArrayList<>())
              .add(
                  new FigurineByMonthResp(
                      figurine.getId(),
                      figurine.getNormalizedName(),
                      resolveFigurineUrl(figurine)));
        });

    return groupedByMonthAndLineUp.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(
            monthEntry -> {
              int month = monthEntry.getKey();

              List<LineUpByMonthResp> lineUp =
                  monthEntry.getValue().entrySet().stream()
                      .map(
                          lineEntry ->
                              new LineUpByMonthResp(
                                  lineEntry.getKey(),
                                  lineEntry.getValue().stream()
                                      .sorted(Comparator.comparing(FigurineByMonthResp::name))
                                      .toList()))
                      .sorted(Comparator.comparing(LineUpByMonthResp::line))
                      .toList();

              return new MonthStatisticsResp(
                  month, Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH), lineUp);
            })
        .toList();
  }

  private boolean isReleased(Figurine figurine) {
    ReleaseStatus status = figurineService.calculateReleaseStatus(figurine);
    return status == RELEASED;
  }

  private Map<String, Integer> countByReleaseStatus(List<Figurine> allFigurines) {
    return allFigurines.stream()
        .map(figurineService::calculateReleaseStatus)
        .collect(Collectors.groupingBy(ReleaseStatus::name, Collectors.summingInt(status -> 1)));
  }

  private <T> Map<String, Integer> countByCatalog(
      List<Figurine> allFigurines,
      List<T> allCatalogs,
      Function<Figurine, T> figurineCatalogSelector,
      Function<T, Long> catalogIdSelector,
      Function<T, String> catalogDescriptionSelector) {

    Map<Long, Long> figurinesByCatalogId =
        allFigurines.stream()
            .map(figurineCatalogSelector)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(catalogIdSelector, Collectors.counting()));

    Map<String, Integer> countByCatalog = new HashMap<>();
    allCatalogs.forEach(
        catalog -> {
          long count = figurinesByCatalogId.getOrDefault(catalogIdSelector.apply(catalog), 0L);
          countByCatalog.put(catalogDescriptionSelector.apply(catalog), (int) count);
        });

    return countByCatalog;
  }

  private Optional<Integer> extractReleaseMonthForYear(Figurine figurine, Integer year) {
    return figurine.getDistributors().stream()
        .map(FigurineDistributor::getReleaseDate)
        .filter(Objects::nonNull)
        .filter(releaseDate -> releaseDate.getYear() == year)
        .map(LocalDate::getMonthValue)
        .min(Comparator.naturalOrder());
  }

  private String resolveFigurineUrl(Figurine figurine) {
    if (figurine.getOfficialImages() != null && !figurine.getOfficialImages().isEmpty()) {
      return figurine.getOfficialImages().getFirst();
    }
    return "";
  }
}
