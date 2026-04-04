package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.figurines.dto.DistributorReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineDistributorResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(FigurineController.class)
class FigurineControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FigurineService service;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void createFigurine_shouldReturn404_whenPostingToRootPath() throws Exception {

    mockMvc
        .perform(post("/"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("The URL you are calling does not exist."))
        .andExpect(jsonPath("$.instance").value("/"))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Endpoint not found"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void createFigurine_shouldReturn400_whenRequestBodyIsMissing() throws Exception {

    mockMvc
        .perform(post("/figurines"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Required request body is missing: public org.springframework.http.ResponseEntity<com.mesofi.mythclothapi.figurines.dto.FigurineResp> com.mesofi.mythclothapi.figurines.FigurineController.createFigurine(com.mesofi.mythclothapi.figurines.dto.FigurineReq)"))
        .andExpect(jsonPath("$.instance").value("/figurines"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void createFigurine_shouldReturn415_whenContentTypeIsMissing() throws Exception {

    mockMvc
        .perform(post("/figurines").content("{}"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(
            jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(jsonPath("$.instance").value("/figurines"))
        .andExpect(jsonPath("$.status").value("415"))
        .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void createFigurine_shouldReturn400_whenRequestBodyFailsValidation() throws Exception {

    mockMvc
        .perform(post("/figurines").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/figurines"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.name").value("must not be blank"))
        .andExpect(
            jsonPath("$.errors.distributors").value("At least one distributor must be provided"))
        .andExpect(jsonPath("$.errors.lineUpId").value("must not be null"))
        .andExpect(jsonPath("$.errors.seriesId").value("must not be null"))
        .andExpect(jsonPath("$.errors.groupId").value("must not be null"));
  }

  @Test
  void createFigurine_shouldReturn400_whenRequestBodyHasOnlyPartialFields() throws Exception {

    mockMvc
        .perform(
            post("/figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Seiya\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/figurines"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(
            jsonPath("$.errors.distributors").value("At least one distributor must be provided"))
        .andExpect(jsonPath("$.errors.lineUpId").value("must not be null"))
        .andExpect(jsonPath("$.errors.seriesId").value("must not be null"))
        .andExpect(jsonPath("$.errors.groupId").value("must not be null"));
  }

  @Test
  void createFigurine_shouldReturn400_whenDistributorsPayloadIsIncomplete() throws Exception {

    mockMvc
        .perform(
            post("/figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Seiya\", \"distributors\":[{}]}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/figurines"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.lineUpId").value("must not be null"))
        .andExpect(jsonPath("$.errors.seriesId").value("must not be null"))
        .andExpect(jsonPath("$.errors.groupId").value("must not be null"));
  }

  @Test
  void createFigurine_shouldReturn400_whenDistributorEntryLacksRequiredFields() throws Exception {

    mockMvc
        .perform(
            post("/figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Seiya\", \"distributors\":[{}],\"lineUpId\":\"3\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/figurines"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.seriesId").value("must not be null"))
        .andExpect(jsonPath("$.errors.groupId").value("must not be null"));
  }

  @Test
  void createFigurine_shouldReturn400_whenRequestBodyMissingGroupId() throws Exception {

    mockMvc
        .perform(
            post("/figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Seiya\", \"distributors\":[{}],\"lineUpId\":\"3\",\"seriesId\":\"2\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/figurines"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.groupId").value("must not be null"));
  }

  @Test
  void createFigurine_shouldReturn400_whenDistributorCurrencyHasUnknownValue() throws Exception {

    mockMvc
        .perform(
            post("/figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Seiya\", \"distributors\":[{\"currency\":\"=\"}],\"lineUpId\":\"3\",\"seriesId\":\"2\", \"groupId\":\"5\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "JSON parse error: Cannot deserialize value of type `com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode` from String \"=\": not one of the values accepted for Enum class: [EUR, MXN, CAD, CNY, JPY, USD]"))
        .andExpect(jsonPath("$.instance").value("/figurines"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void createFigurine_shouldReturn201AndLocationHeader() throws Exception {
    FigurineReq request = createFigurineRequest();
    FigurineResp response = createFigurineResponse(1L, "Pegasus Seiya");

    when(service.createFigurine(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/figurines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Pegasus Seiya"));

    verify(service).createFigurine(any());
  }

  @Test
  void retrieveFigurine_shouldReturn200_whenFigurineExists() throws Exception {
    FigurineResp response = createFigurineResponse(1L, "Pegasus Seiya");

    when(service.readFigurine(1L)).thenReturn(response);

    mockMvc
        .perform(get("/figurines/{id}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Pegasus Seiya"));

    verify(service).readFigurine(1L);
  }

  @Test
  void retrieveFigurines_shouldReturnPaginatedPayload_whenPageAndSizeAreProvided()
      throws Exception {
    FigurineResp first = createFigurineResponse(1L, "Pegasus Seiya");
    FigurineResp second = createFigurineResponse(2L, "Dragon Shiryu");
    PageRequest pageRequest = PageRequest.of(0, 2);

    when(service.readFigurines(0, 2))
        .thenReturn(new PageImpl<>(List.of(first, second), pageRequest, 5));

    mockMvc
        .perform(get("/figurines").param("page", "0").param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.totalElements").value(5))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.content.length()").value(2));

    verify(service).readFigurines(0, 2);
  }

  @Test
  void retrieveFigurines_shouldReturnBadRequest_whenPageIsNegative() throws Exception {
    assertThatThrownBy(
            () -> mockMvc.perform(get("/figurines").param("page", "-1").param("size", "10")))
        .hasRootCauseInstanceOf(jakarta.validation.ConstraintViolationException.class);
  }

  @Test
  void updateFigurine_shouldReturn200_whenRequestIsValid() throws Exception {
    FigurineReq request = createFigurineRequest();
    FigurineResp response = createFigurineResponse(1L, "Dragon Shiryu");

    when(service.updateFigurine(1L, request)).thenReturn(response);

    mockMvc
        .perform(
            put("/figurines/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Dragon Shiryu"));

    verify(service).updateFigurine(1L, request);
  }

  @Test
  void deleteFigurine_shouldReturn204_whenFigurineExists() throws Exception {
    mockMvc.perform(delete("/figurines/{id}", 1L)).andExpect(status().isNoContent());

    verify(service).deleteFigurine(1L);
  }

  private FigurineReq createFigurineRequest() {
    return new FigurineReq(
        "Pegasus Seiya",
        List.of(new DistributorReq(1L, JPY, 16000d, null, null, null, null)),
        "https://tamashiiweb.com/item/12345",
        2L,
        1L,
        1L,
        1L,
        null,
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        true,
        "Bronze Saint",
        List.of("https://images.example/pegasus.jpg"),
        List.of("https://images.example/pegasus-fan.jpg"));
  }

  private FigurineResp createFigurineResponse(long id, String name) {
    return new FigurineResp(
        id,
        name,
        name + " Myth Cloth EX",
        List.of(
            new FigurineDistributorResp(
                new DistributorResp(1L, "BANDAI", "Tamashii Nations", "JP", null),
                JPY,
                16000d,
                17600d,
                null,
                null,
                null,
                false)),
        "https://tamashiiweb.com/item/12345",
        new CatalogResp(2L, "Tamashii Nations"),
        new CatalogResp(1L, "Myth Cloth EX"),
        new CatalogResp(1L, "Saint Seiya"),
        new CatalogResp(1L, "Bronze Saint V3"),
        null,
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        true,
        "Bronze Saint",
        List.of("https://images.example/pegasus.jpg"),
        List.of("https://images.example/pegasus-fan.jpg"),
        List.of(),
        Instant.parse("2026-01-01T00:00:00Z"),
        Instant.parse("2026-01-02T00:00:00Z"));
  }
}
