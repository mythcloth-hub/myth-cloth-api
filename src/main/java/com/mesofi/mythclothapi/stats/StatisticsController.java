package com.mesofi.mythclothapi.stats;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.FigurineFilterFactory;
import com.mesofi.mythclothapi.stats.dto.StatisticsResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatisticsController {

  private final StatisticsService service;

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
}
