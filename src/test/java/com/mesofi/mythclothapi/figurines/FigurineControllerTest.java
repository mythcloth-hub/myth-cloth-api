package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.containsDetail;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.defaultType;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasDetail;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasErrors;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasInstance;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasStatus;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasTimestamp;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasTitle;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.catalogs.dto.AnniversaryResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurines.dto.FigurineDistributorResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.utils.MethodFileSource;

@WebMvcTest(FigurineController.class)
public class FigurineControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean FigurineService service;

  @Test
  void createFigurine_shouldReturn400_whenBodyIsMissing() throws Exception {
    mockMvc
        .perform(post("/figurines"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Invalid body"))
        .andExpect(hasStatus(400))
        .andExpect(containsDetail("Required request body is missing"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp());
  }

  @Test
  void createFigurine_shouldReturn415_whenBodyIsText() throws Exception {
    mockMvc
        .perform(post("/figurines").content("The Body"))
        .andDo(print())
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(defaultType())
        .andExpect(hasTitle("Unsupported Media Type"))
        .andExpect(hasStatus(415))
        .andExpect(hasDetail("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp());
  }

  @Test
  void createFigurine_shouldReturn400_whenBodyIsUnparseable() throws Exception {
    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content("The Body"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Invalid body"))
        .andExpect(hasStatus(400))
        .andExpect(
            hasDetail(
                "JSON parse error: Unrecognized token 'The': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp());
  }

  @ParameterizedTest
  @MethodFileSource(folder = "/figurines/request")
  void createFigurine_shouldReturn400_whenRequestIsEmpty(String jsonRequest) throws Exception {
    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp())
        .andExpect(
            hasErrors(
                Map.of(
                    "distributionId",
                    "must not be null",
                    "lineUpId",
                    "must not be null",
                    "distributors",
                    "At least one distributor must be provided",
                    "groupId",
                    "must not be null",
                    "name",
                    "must not be blank",
                    "seriesId",
                    "must not be null")));
  }

  @ParameterizedTest
  @MethodFileSource(folder = "/figurines/request")
  void createFigurine_shouldReturn400_whenDistributorIsEmpty(String jsonRequest) throws Exception {
    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp())
        .andExpect(
            hasErrors(
                Map.of(
                    "distributionId",
                    "must not be null",
                    "lineUpId",
                    "must not be null",
                    "distributors",
                    "At least one distributor must be provided",
                    "groupId",
                    "must not be null",
                    "name",
                    "must not be blank",
                    "seriesId",
                    "must not be null")));
  }

  @ParameterizedTest
  @MethodFileSource(folder = "/figurines/request")
  void createFigurine_shouldReturn400_whenDistributorSupplierIdIsNegative(String jsonRequest)
      throws Exception {
    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp())
        .andExpect(
            hasErrors(
                Map.of(
                    "distributionId",
                    "must not be null",
                    "lineUpId",
                    "must not be null",
                    "groupId",
                    "must not be null",
                    "name",
                    "must not be blank",
                    "seriesId",
                    "must not be null",
                    "distributors[0].supplierId",
                    "must be greater than 0")));
  }

  @ParameterizedTest
  @MethodFileSource(folder = "/figurines/request")
  void createFigurine_shouldReturn400_whenDistributorPriceIsNegative(String jsonRequest)
      throws Exception {
    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp())
        .andExpect(
            hasErrors(
                Map.of(
                    "distributionId",
                    "must not be null",
                    "lineUpId",
                    "must not be null",
                    "groupId",
                    "must not be null",
                    "name",
                    "must not be blank",
                    "seriesId",
                    "must not be null",
                    "distributors[0].price",
                    "must be greater than 0")));
  }

  @ParameterizedTest
  @MethodFileSource(folder = "/figurines/request")
  void createFigurine_shouldReturn400_whenDistributorCurrencyIsInvalid(String jsonRequest)
      throws Exception {
    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Invalid body"))
        .andExpect(hasStatus(400))
        .andExpect(
            hasDetail(
                "JSON parse error: Cannot deserialize value of type `com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode` from String \"-\": not one of the values accepted for Enum class: [EUR, JPY, USD, MXN, CNY]"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp());
  }

  @ParameterizedTest
  @MethodFileSource(folder = "/figurines/request")
  void createFigurine_shouldReturn400_whenDistributorDateIsInvalid(String jsonRequest)
      throws Exception {
    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Invalid body"))
        .andExpect(hasStatus(400))
        .andExpect(
            hasDetail(
                "JSON parse error: Cannot deserialize value of type `java.time.LocalDate` from String \"-\": Failed to deserialize java.time.LocalDate: (java.time.format.DateTimeParseException) Text '-' could not be parsed at index 1"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp());
  }

  @ParameterizedTest
  @MethodFileSource(folder = "/figurines/request")
  void createFigurine_shouldReturn400_whenNameIsTooLong(String jsonRequest) throws Exception {
    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp())
        .andExpect(
            hasErrors(
                Map.of(
                    "distributionId",
                    "must not be null",
                    "lineUpId",
                    "must not be null",
                    "groupId",
                    "must not be null",
                    "name",
                    "Name must not exceed 100 characters",
                    "seriesId",
                    "must not be null")));
  }

  @ParameterizedTest
  @MethodFileSource(folder = "/figurines/request")
  void createFigurine_shouldReturn400_whenDistributionLineupGroupAndSeriesAreNull(
      String jsonRequest) throws Exception {
    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp())
        .andExpect(
            hasErrors(
                Map.of(
                    "distributionId",
                    "must not be null",
                    "lineUpId",
                    "must not be null",
                    "groupId",
                    "must not be null",
                    "seriesId",
                    "must not be null")));
  }

  @ParameterizedTest
  @MethodFileSource(folder = "/figurines/request")
  void createFigurine_shouldReturn400_whenLineupGroupAndSeriesAreNull(String jsonRequest)
      throws Exception {
    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp())
        .andExpect(
            hasErrors(
                Map.of(
                    "lineUpId",
                    "must not be null",
                    "groupId",
                    "must not be null",
                    "seriesId",
                    "must not be null")));
  }

  @ParameterizedTest
  @MethodFileSource(folder = "/figurines/request")
  void createFigurine_shouldReturn400_whenGroupAndSeriesIdAreNull(String jsonRequest)
      throws Exception {
    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp())
        .andExpect(
            hasErrors(Map.of("groupId", "must not be null", "seriesId", "must not be null")));
  }

  @ParameterizedTest
  @MethodFileSource(folder = "/figurines/request")
  void createFigurine_shouldReturn400_whenGroupIdIsNull(String jsonRequest) throws Exception {
    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance("/figurines"))
        .andExpect(hasTimestamp())
        .andExpect(hasErrors(Map.of("groupId", "must not be null")));
  }

  @ParameterizedTest
  @MethodFileSource(folder = "/figurines/request", type = FigurineReq.class)
  void createFigurine_shouldReturn200_whenRequestIsValid(String jsonRequest, FigurineReq req)
      throws Exception {
    AnniversaryResp anniversaryResp = new AnniversaryResp();
    anniversaryResp.setId(5);
    anniversaryResp.setDescription("Masami Kurumada 40th Anniversary");
    anniversaryResp.setYear(40);

    when(service.createFigurine(req))
        .thenReturn(
            new FigurineResp(
                3,
                "Pegasus Seiya",
                "Pegasus Seiya [Final Bronze Cloth] ~Original Color Edition~",
                List.of(createFigurineDistributor1(), createFigurineDistributor2()),
                "https://tamashiiweb.com/item/15695",
                new CatalogResp(6, "Tamashii Web Shop"),
                new CatalogResp(3, "Myth Cloth EX"),
                new CatalogResp(7, "Saint Seiya"),
                new CatalogResp(12, "Bronze Saint V3"),
                anniversaryResp,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                "Shown for the first time in Tamashii 2025",
                List.of(
                    "https://imagizer.imageshack.com/img923/5373/mM9Vnw.jpg",
                    "https://imagizer.imageshack.com/img923/5718/9ekW8o.jpg",
                    "https://imagizer.imageshack.com/img922/8738/4a9Xam.jpg"),
                List.of("https://imagizer.imageshack.com/img922/5273/9ZUPz6.jpg"),
                List.of(createFigurineEvents1(), createFigurineEvents2()),
                Instant.ofEpochSecond(1708886400L),
                Instant.ofEpochSecond(1708886400L)));

    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(jsonRequest))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/figurines/3"))
        .andExpect(jsonPath("$.id").value(3))
        .andExpect(jsonPath("$.name").value("Pegasus Seiya"))
        .andExpect(
            jsonPath("$.displayableName")
                .value("Pegasus Seiya [Final Bronze Cloth] ~Original Color Edition~"))
        .andExpect(jsonPath("$.distributors[0].distributor.id").value(23))
        .andExpect(jsonPath("$.distributors[0].distributor.name").value("BANDAI"))
        .andExpect(jsonPath("$.distributors[0].currency").value("JPY"))
        .andExpect(jsonPath("$.distributors[0].price").value(16000.0))
        .andExpect(jsonPath("$.distributors[0].announcedAt").value("2025-11-13"))
        .andExpect(jsonPath("$.distributors[0].releaseDateConfirmed").value(false))
        .andExpect(jsonPath("$.distributors[1].distributor.id").value(32))
        .andExpect(jsonPath("$.distributors[1].currency").value("MXN"))
        .andExpect(jsonPath("$.distributors[1].price").value(5200.0))
        .andExpect(jsonPath("$.distributors[1].preorderOpensAt").value("2025-12-14"))
        .andExpect(jsonPath("$.distributors[1].releaseDateConfirmed").value(true))
        .andExpect(jsonPath("$.distribution.id").value(6))
        .andExpect(jsonPath("$.lineUp.id").value(3))
        .andExpect(jsonPath("$.series.id").value(7))
        .andExpect(jsonPath("$.group.id").value(12))
        .andExpect(jsonPath("$.anniversary.id").value(5))
        .andExpect(jsonPath("$.anniversary.year").value(40))
        .andExpect(jsonPath("$.isOriginalColorEdition").value(true))
        .andExpect(jsonPath("$.isArticulable").value(true))
        .andExpect(jsonPath("$.notes").value("Shown for the first time in Tamashii 2025"))
        .andExpect(jsonPath("$.officialImageUrls", hasSize(3)))
        .andExpect(
            jsonPath("$.officialImageUrls[0]")
                .value("https://imagizer.imageshack.com/img923/5373/mM9Vnw.jpg"))
        .andExpect(jsonPath("$.unofficialImageUrls", hasSize(1)))
        .andExpect(
            jsonPath("$.unofficialImageUrls[0]")
                .value("https://imagizer.imageshack.com/img922/5273/9ZUPz6.jpg"))
        .andExpect(jsonPath("$.events", hasSize(2)))
        .andExpect(jsonPath("$.events[0].type").value("ANNOUNCEMENT"))
        .andExpect(jsonPath("$.events[1].type").value("PREORDER_OPEN"))
        .andExpect(jsonPath("$.createdAt").value("2024-02-25T18:40:00Z"))
        .andExpect(jsonPath("$.updatedAt").value("2024-02-25T18:40:00Z"));
  }

  private FigurineEventResp createFigurineEvents1() {
    return new FigurineEventResp(
        1,
        LocalDate.of(2025, 11, 13),
        FigurineEventType.ANNOUNCEMENT,
        CountryCode.JP,
        "Revealed at Tamashii Nations 2025.",
        null);
  }

  private FigurineEventResp createFigurineEvents2() {
    return new FigurineEventResp(
        2,
        LocalDate.of(2025, 5, 12),
        FigurineEventType.PREORDER_OPEN,
        CountryCode.JP,
        "Preorders open.",
        null);
  }

  private FigurineDistributorResp createFigurineDistributor1() {
    return new FigurineDistributorResp(
        new DistributorResp(23, "BANDAI", "Tamashii Nations", "JP", "https://tamashii.jp/"),
        CurrencyCode.JPY,
        16000.00,
        17600.00,
        LocalDate.of(2025, 11, 13),
        LocalDate.of(2025, 5, 12),
        LocalDate.of(2026, 5, 1),
        false);
  }

  private FigurineDistributorResp createFigurineDistributor2() {
    return new FigurineDistributorResp(
        new DistributorResp(
            32, "DAM", "Distribuidora Animéxico", "MX", "https://animexico-online.com/"),
        CurrencyCode.MXN,
        5200.00,
        null,
        LocalDate.of(2025, 12, 13),
        LocalDate.of(2025, 12, 14),
        LocalDate.of(2026, 1, 1),
        true);
  }
}
