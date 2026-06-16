package com.mesofi.mythclothapi.stats;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.FigurineFilterFactory;
import com.mesofi.mythclothapi.stats.dto.MonthStatisticsResp;
import com.mesofi.mythclothapi.stats.dto.StatisticsResp;
import com.mesofi.mythclothapi.stats.dto.YearReleasePriceResp;
import com.mesofi.mythclothapi.stats.dto.YearStatisticsResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller exposing figurine statistics endpoints.
 *
 * <p>Endpoints support optional filtering through query parameters that are converted into a {@link
 * FigurineFilter} before delegating to {@link StatisticsService}.
 */
@Slf4j
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('stats:read')")
public class StatisticsController {

  private final StatisticsService service;

  /**
   * Retrieves global figurine statistics based on optional filters.
   *
   * @param name figurine name fragment filter
   * @param lineUpId line-up id filter
   * @param seriesId series id filter
   * @param groupId group id filter
   * @param anniversaryId anniversary id filter
   * @param metalBody metal body flag filter
   * @param oce OCE flag filter
   * @param revival revival flag filter
   * @param plainCloth plain cloth flag filter
   * @param broken broken status flag filter
   * @param golden golden status flag filter
   * @param gold gold status flag filter
   * @param manga manga version flag filter
   * @param set set flag filter
   * @param articulable articulation flag filter
   * @param releaseStatus release status filter
   * @return global statistics response
   */
  @GetMapping
  public StatisticsResp retrieveStatistics(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Long lineUpId,
      @RequestParam(required = false) Long seriesId,
      @RequestParam(required = false) Long groupId,
      @RequestParam(required = false) Long anniversaryId,
      @RequestParam(required = false) Boolean metalBody,
      @RequestParam(required = false) Boolean oce,
      @RequestParam(required = false) Boolean revival,
      @RequestParam(required = false) Boolean plainCloth,
      @RequestParam(required = false) Boolean broken,
      @RequestParam(required = false) Boolean golden,
      @RequestParam(required = false) Boolean gold,
      @RequestParam(required = false) Boolean manga,
      @RequestParam(required = false) Boolean set,
      @RequestParam(required = false) Boolean articulable,
      @RequestParam(required = false) String releaseStatus) {

    FigurineFilter figurineFilter =
        FigurineFilterFactory.build(
            name,
            lineUpId,
            seriesId,
            groupId,
            anniversaryId,
            metalBody,
            oce,
            revival,
            plainCloth,
            broken,
            golden,
            gold,
            manga,
            set,
            articulable,
            releaseStatus);

    return service.retrieveStatistics(figurineFilter);
  }

  /**
   * Retrieves yearly release counts grouped by line-up, based on optional filters.
   *
   * @param name figurine name fragment filter
   * @param lineUpId line-up id filter
   * @param seriesId series id filter
   * @param groupId group id filter
   * @param anniversaryId anniversary id filter
   * @param metalBody metal body flag filter
   * @param oce OCE flag filter
   * @param revival revival flag filter
   * @param plainCloth plain cloth flag filter
   * @param broken broken status flag filter
   * @param golden golden status flag filter
   * @param gold gold status flag filter
   * @param manga manga version flag filter
   * @param set set flag filter
   * @param articulable articulation flag filter
   * @return list of yearly release statistics
   */
  @GetMapping("/releases/years")
  public List<YearStatisticsResp> retrieveStatisticsByReleases(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Long lineUpId,
      @RequestParam(required = false) Long seriesId,
      @RequestParam(required = false) Long groupId,
      @RequestParam(required = false) Long anniversaryId,
      @RequestParam(required = false) Boolean metalBody,
      @RequestParam(required = false) Boolean oce,
      @RequestParam(required = false) Boolean revival,
      @RequestParam(required = false) Boolean plainCloth,
      @RequestParam(required = false) Boolean broken,
      @RequestParam(required = false) Boolean golden,
      @RequestParam(required = false) Boolean gold,
      @RequestParam(required = false) Boolean manga,
      @RequestParam(required = false) Boolean set,
      @RequestParam(required = false) Boolean articulable) {

    FigurineFilter figurineFilter =
        FigurineFilterFactory.build(
            name,
            lineUpId,
            seriesId,
            groupId,
            anniversaryId,
            metalBody,
            oce,
            revival,
            plainCloth,
            broken,
            golden,
            gold,
            manga,
            set,
            articulable,
            null);

    return service.retrieveStatisticsByReleases(figurineFilter);
  }

  /**
   * Retrieves month-by-month release statistics for a specific year.
   *
   * @param year year to inspect
   * @return monthly statistics for the requested year
   */
  @GetMapping("/releases/years/{year}")
  public List<MonthStatisticsResp> retrieveStatisticsByYear(@PathVariable Integer year) {
    return service.retrieveStatisticsByYear(year);
  }

  /**
   * Retrieves yearly release-price statistics based on optional filters.
   *
   * @param name figurine name fragment filter
   * @param lineUpId line-up id filter
   * @param seriesId series id filter
   * @param groupId group id filter
   * @param anniversaryId anniversary id filter
   * @param metalBody metal body flag filter
   * @param oce OCE flag filter
   * @param revival revival flag filter
   * @param plainCloth plain cloth flag filter
   * @param broken broken status flag filter
   * @param golden golden status flag filter
   * @param gold gold status flag filter
   * @param manga manga version flag filter
   * @param set set flag filter
   * @param articulable articulation flag filter
   * @param releaseStatus release status filter
   * @return list of yearly release-price statistics
   */
  @GetMapping("/prices/releases/years")
  public List<YearReleasePriceResp> retrieveYearlyReleasePrices(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Long lineUpId,
      @RequestParam(required = false) Long seriesId,
      @RequestParam(required = false) Long groupId,
      @RequestParam(required = false) Long anniversaryId,
      @RequestParam(required = false) Boolean metalBody,
      @RequestParam(required = false) Boolean oce,
      @RequestParam(required = false) Boolean revival,
      @RequestParam(required = false) Boolean plainCloth,
      @RequestParam(required = false) Boolean broken,
      @RequestParam(required = false) Boolean golden,
      @RequestParam(required = false) Boolean gold,
      @RequestParam(required = false) Boolean manga,
      @RequestParam(required = false) Boolean set,
      @RequestParam(required = false) Boolean articulable,
      @RequestParam(required = false) String releaseStatus) {

    FigurineFilter figurineFilter =
        FigurineFilterFactory.build(
            name,
            lineUpId,
            seriesId,
            groupId,
            anniversaryId,
            metalBody,
            oce,
            revival,
            plainCloth,
            broken,
            golden,
            gold,
            manga,
            set,
            articulable,
            releaseStatus);

    return service.retrieveYearlyReleasePrices(figurineFilter);
  }
}
