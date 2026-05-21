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
import java.util.TreeMap;
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
 * Service responsible for generating aggregated figurine statistics used by the stats endpoints.
 *
 * <p>It provides:
 *
 * <ul>
 *   <li>global counters by catalog and release status,
 *   <li>yearly release totals grouped by line-up,
 *   <li>monthly release breakdown for a specific year.
 * </ul>
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
   * Retrieves a global statistics snapshot for figurines that match the given filter.
   *
   * @param filter search filter used to constrain figurines included in the aggregation
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
   * Retrieves yearly release statistics grouped by line-up description.
   *
   * <p>Only figurines currently evaluated as {@code RELEASED} are considered. Each figurine is
   * counted against the release year of its first distributor record when present.
   *
   * @param filter search filter used to constrain figurines included in the aggregation
   * @return list of yearly aggregates sorted by year (ascending)
   */
  public List<YearStatisticsResp> retrieveStatisticsByReleases(@NotNull FigurineFilter filter) {
    List<Figurine> allFigurines =
        repository.findAll(filter).stream()
            .filter(figurine -> figurineService.calculateReleaseStatus(figurine) == RELEASED)
            .toList();

    Map<Integer, Map<String, Integer>> map = new TreeMap<>();

    for (Figurine figurine : allFigurines) {
      Optional<FigurineDistributor> s = figurine.getDistributors().stream().findFirst();
      if (s.isPresent()) {
        int theYear = s.get().getReleaseDate().getYear();
        String description = figurine.getLineup().getDescription();

        if (map.containsKey(theYear)) {
          Map<String, Integer> mm = map.get(theYear);
          mm.merge(description, 1, Integer::sum);
        } else {
          Map<String, Integer> map1 = new HashMap<>();
          map1.put(description, 1);
          map.put(theYear, map1);
        }
      }
    }

    List<YearStatisticsResp> resp = new ArrayList<>();
    map.forEach(
        (year, lineUpMap) -> {
          List<LineUpCountResp> lineUp = new ArrayList<>();
          lineUpMap.forEach((line, count) -> lineUp.add(new LineUpCountResp(line, count)));

          resp.add(new YearStatisticsResp(year, lineUp));
        });

    return resp;
  }

  /**
   * Retrieves monthly release statistics for a specific year.
   *
   * <p>Results are grouped first by month and then by line-up, with figurines sorted by normalized
   * name inside each line-up.
   *
   * @param year year to inspect
   * @return month-based release breakdown for the requested year
   */
  public List<MonthStatisticsResp> retrieveStatisticsByYear(int year) {
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
