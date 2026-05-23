package com.mesofi.mythclothapi.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.stats.dto.FigurineByMonthResp;
import com.mesofi.mythclothapi.stats.dto.FigurinePriceResp;
import com.mesofi.mythclothapi.stats.dto.LineUpByMonthResp;
import com.mesofi.mythclothapi.stats.dto.LineUpCountResp;
import com.mesofi.mythclothapi.stats.dto.MonthStatisticsResp;
import com.mesofi.mythclothapi.stats.dto.StatisticsResp;
import com.mesofi.mythclothapi.stats.dto.YearReleasePriceResp;
import com.mesofi.mythclothapi.stats.dto.YearStatisticsResp;

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private StatisticsService service;

  @Test
  void retrieveStatistics_shouldReturnAggregatedValues() throws Exception {
    when(service.retrieveStatistics(any()))
        .thenReturn(
            new StatisticsResp(
                2,
                Map.of("Myth Cloth EX", 2),
                Map.of("Hades", 2),
                Map.of(),
                Map.of(),
                Map.of("RELEASED", 1, "ANNOUNCED", 1)));

    mockMvc
        .perform(
            get("/stats")
                .param("name", "  Gemini Saga  ")
                .param("lineUpId", "10")
                .param("releaseStatus", "RELEASED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalFigurines").value(2))
        .andExpect(jsonPath("$.countByLineUp['Myth Cloth EX']").value(2))
        .andExpect(jsonPath("$.countBySeries.Hades").value(2))
        .andExpect(jsonPath("$.totalByReleaseStatus.RELEASED").value(1))
        .andExpect(jsonPath("$.totalByReleaseStatus.ANNOUNCED").value(1));

    ArgumentCaptor<FigurineFilter> captor = ArgumentCaptor.forClass(FigurineFilter.class);
    verify(service).retrieveStatistics(captor.capture());
    assertThat(captor.getValue().name()).isEqualTo("Gemini Saga");
    assertThat(captor.getValue().lineUpId()).isEqualTo(10L);
    assertThat(captor.getValue().releaseStatus()).isEqualTo("RELEASED");
  }

  @Test
  void retrieveStatisticsByReleases_shouldReturnYearlyList() throws Exception {
    when(service.retrieveStatisticsByReleases(any()))
        .thenReturn(List.of(new YearStatisticsResp(2026, List.of(new LineUpCountResp("EX", 3)))));

    mockMvc
        .perform(get("/stats/releases/years").param("name", "Mu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].year").value(2026))
        .andExpect(jsonPath("$[0].lineUp[0].line").value("EX"))
        .andExpect(jsonPath("$[0].lineUp[0].count").value(3));

    ArgumentCaptor<FigurineFilter> captor = ArgumentCaptor.forClass(FigurineFilter.class);
    verify(service).retrieveStatisticsByReleases(captor.capture());
    assertThat(captor.getValue().name()).isEmpty();
    assertThat(captor.getValue().releaseStatus()).isNull();
  }

  @Test
  void retrieveStatisticsByYear_shouldReturnMonthsList() throws Exception {
    when(service.retrieveStatisticsByYear(2026))
        .thenReturn(
            List.of(
                new MonthStatisticsResp(
                    1,
                    "January",
                    List.of(
                        new LineUpByMonthResp(
                            "Myth Cloth EX",
                            List.of(
                                new FigurineByMonthResp(
                                    10L, "Gemini Saga", "https://img/saga.jpg")))))));

    mockMvc
        .perform(get("/stats/releases/years/{year}", 2026))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].month").value(1))
        .andExpect(jsonPath("$[0].name").value("January"))
        .andExpect(jsonPath("$[0].lineUp[0].line").value("Myth Cloth EX"))
        .andExpect(jsonPath("$[0].lineUp[0].figurines[0].id").value(10))
        .andExpect(jsonPath("$[0].lineUp[0].figurines[0].name").value("Gemini Saga"));

    verify(service).retrieveStatisticsByYear(2026);
  }

  @Test
  void retrieveYearlyReleasePrices_shouldReturnYearlyPriceList() throws Exception {
    when(service.retrieveYearlyReleasePrices(any()))
        .thenReturn(
            List.of(
                new YearReleasePriceResp(
                    2025,
                    new BigDecimal("13000.00"),
                    new BigDecimal("15000.00"),
                    new BigDecimal("11000.00"),
                    new FigurinePriceResp(101L, "Dohko", "https://img/dohko.jpg"),
                    new FigurinePriceResp(100L, "Aldebaran", "https://img/aldebaran.jpg"),
                    2)));

    mockMvc
        .perform(
            get("/stats/prices/releases/years")
                .param("name", " Saga ")
                .param("releaseStatus", "RELEASED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].year").value(2025))
        .andExpect(jsonPath("$[0].averageReleasePrice").value(13000.00))
        .andExpect(jsonPath("$[0].highestReleasePrice").value(15000.00))
        .andExpect(jsonPath("$[0].lowestReleasePrice").value(11000.00))
        .andExpect(jsonPath("$[0].highestPriceFigurines.id").value(101))
        .andExpect(jsonPath("$[0].lowestPriceFigurines.id").value(100))
        .andExpect(jsonPath("$[0].releaseCount").value(2));

    ArgumentCaptor<FigurineFilter> captor = ArgumentCaptor.forClass(FigurineFilter.class);
    verify(service).retrieveYearlyReleasePrices(captor.capture());
    assertThat(captor.getValue().name()).isEqualTo("Saga");
    assertThat(captor.getValue().releaseStatus()).isEqualTo("RELEASED");
  }
}
