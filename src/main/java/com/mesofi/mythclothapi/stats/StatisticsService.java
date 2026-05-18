package com.mesofi.mythclothapi.stats;

import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.ANNOUNCED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.RELEASED;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.mesofi.mythclothapi.stats.dto.StatisticsResp;
import com.mesofi.mythclothapi.stats.dto.YearStatisticsResp;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsService {

  private final FigurineService figurineService;
  private final FigurineRepository repository;
  private final LineUpRepository lineUpRepository;
  private final SeriesRepository seriesRepository;
  private final GroupRepository groupRepository;
  private final AnniversaryRepository anniversaryRepository;

  public StatisticsResp retrieveStatistics(@NotNull FigurineFilter filter) {
    List<Figurine> allFigurines = repository.findAllFigurines(filter);

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

  public List<YearStatisticsResp> retrieveStatisticsByReleases(@NotNull FigurineFilter filter) {
    List<Figurine> allFigurines = repository.findAllFigurines(filter);
    final int startingYear = 2003;
    final int endingYear = LocalDate.now().getYear() + 1;
    List<LineUp> allLineUps = lineUpRepository.findAll();

    Map<Long, String> lineUpDescById =
        allLineUps.stream().collect(Collectors.toMap(LineUp::getId, LineUp::getDescription));

    Map<Integer, Map<Long, Integer>> countByYearAndLineUp = new HashMap<>();

    allFigurines.stream()
        .filter(this::isReleasedOrAnnounced)
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
      Map<String, Integer> countByLineUp = new HashMap<>();
      lineUpDescById.forEach(
          (lineUpId, description) ->
              countByLineUp.put(description, yearlyByLineUp.getOrDefault(lineUpId, 0)));
      respList.add(new YearStatisticsResp(currYear, countByLineUp));
    }

    return respList;
  }

  private boolean isReleasedOrAnnounced(Figurine figurine) {
    ReleaseStatus status = figurineService.calculateReleaseStatus(figurine);
    return status == RELEASED || status == ANNOUNCED;
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
}
