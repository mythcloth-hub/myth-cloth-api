package com.mesofi.mythclothapi.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.FigurineService;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;
import com.mesofi.mythclothapi.stats.dto.StatisticsResp;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsService {

  private final FigurineService figurineService;
  private final FigurineRepository repository;
  private final LineUpRepository lineUpRepository;
  private final SeriesRepository seriesRepository;

  public StatisticsResp retrieveStatistics(@NotNull FigurineFilter filter) {

    List<Figurine> allFigurines = repository.findAllFigurines(filter);

    Map<String, Integer> countByReleaseStatus =
        allFigurines.stream()
            .map(figurineService::calculateReleaseStatus)
            .collect(
                Collectors.groupingBy(ReleaseStatus::name, Collectors.summingInt(status -> 1)));

    // Group figurines by lineUpId for efficient counting
    Map<Long, Long> figurinesByLineUpId =
        allFigurines.stream()
            .collect(Collectors.groupingBy(fig -> fig.getLineup().getId(), Collectors.counting()));

    Map<Long, Long> figurinesBySeriesId =
        allFigurines.stream()
            .collect(Collectors.groupingBy(fig -> fig.getSeries().getId(), Collectors.counting()));

    Map<String, Integer> countByLineUp = new HashMap<>();
    lineUpRepository
        .findAll()
        .forEach(
            line -> {
              long count = figurinesByLineUpId.getOrDefault(line.getId(), 0L);
              countByLineUp.put(line.getDescription(), (int) count);
            });

    Map<String, Integer> countBySeries = new HashMap<>();
    seriesRepository
        .findAll()
        .forEach(
            series -> {
              long count = figurinesBySeriesId.getOrDefault(series.getId(), 0L);
              countBySeries.put(series.getDescription(), (int) count);
            });

    return new StatisticsResp(
        allFigurines.size(), countByReleaseStatus, countByLineUp, countBySeries);
  }
}
