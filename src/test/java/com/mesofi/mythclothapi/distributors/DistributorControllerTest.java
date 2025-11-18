package com.mesofi.mythclothapi.distributors;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.DistributorName.BANDAI;
import static com.mesofi.mythclothapi.utils.CommonAssertions.hasDescription;
import static com.mesofi.mythclothapi.utils.CommonAssertions.hasId;
import static com.mesofi.mythclothapi.utils.CommonAssertions.hasName;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.containsDetail;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.defaultType;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasDetail;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasErrors;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasInstance;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasStatus;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasTimestamp;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasTitle;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorNotFoundException;
import com.mesofi.mythclothapi.distributors.model.DistributorRequest;
import com.mesofi.mythclothapi.distributors.model.DistributorResponse;

@WebMvcTest(DistributorController.class)
public class DistributorControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean DistributorService service;

  private final DistributorRequest mockRequest =
      new DistributorRequest(BANDAI, JP, "https://tamashiiweb.com/");

  @ParameterizedTest
  @MethodSource("provideHttpRequestsAndExpectedPaths")
  void shouldReturn404_whenEndpointsDoNotExist(RequestBuilder requestBuilder, String invalidUri)
      throws Exception {
    mockMvc
        .perform(requestBuilder)
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(defaultType())
        .andExpect(hasTitle("Endpoint not found"))
        .andExpect(hasStatus(404))
        .andExpect(hasDetail("The URL you are calling does not exist."))
        .andExpect(hasInstance(invalidUri))
        .andExpect(hasTimestamp());
  }

  private static Stream<Arguments> provideHttpRequestsAndExpectedPaths() {
    return Stream.of(
        Arguments.of(post("/unknown"), "/unknown"),
        Arguments.of(get("/distributor"), "/distributor"),
        Arguments.of(put("/distribution"), "/distribution"),
        Arguments.of(delete("/"), "/"));
  }

  @Test
  void shouldReturn400_whenBodyIsMissing() throws Exception {
    mockMvc
        .perform(post("/distributors"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Invalid body"))
        .andExpect(hasStatus(400))
        .andExpect(containsDetail("Required request body is missing"))
        .andExpect(hasInstance("/distributors"))
        .andExpect(hasTimestamp());
  }

  @Test
  void shouldReturn415_whenBodyIsText() throws Exception {
    mockMvc
        .perform(post("/distributors").content("The Body"))
        .andDo(print())
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(defaultType())
        .andExpect(hasTitle("Unsupported Media Type"))
        .andExpect(hasStatus(415))
        .andExpect(hasDetail("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(hasInstance("/distributors"))
        .andExpect(hasTimestamp());
  }

  @Test
  void shouldReturn400_whenBodyIsUnparseable() throws Exception {
    mockMvc
        .perform(post("/distributors").contentType(APPLICATION_JSON).content("The Body"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Invalid body"))
        .andExpect(hasStatus(400))
        .andExpect(
            hasDetail(
                "JSON parse error: Unrecognized token 'The': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')"))
        .andExpect(hasInstance("/distributors"))
        .andExpect(hasTimestamp());
  }

  @Test
  void shouldReturn400_whenBodyIsEmpty() throws Exception {
    mockMvc
        .perform(post("/distributors").contentType(APPLICATION_JSON).content("{}"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance("/distributors"))
        .andExpect(hasTimestamp())
        .andExpect(
            hasErrors(Map.of("country", "country is required", "name", "name must not be blank")));
  }

  @Test
  void shouldReturn400_whenNameIsInvalid() throws Exception {
    mockMvc
        .perform(post("/distributors").contentType(APPLICATION_JSON).content("{\"name\":\"-\"}"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Invalid body"))
        .andExpect(hasStatus(400))
        .andExpect(
            containsDetail(
                "not one of the values accepted for Enum class: [DAM, BANDAI, BLUE_FIN, DS_DISTRIBUTIONS, DTM]"))
        .andExpect(hasInstance("/distributors"))
        .andExpect(hasTimestamp());
  }

  @Test
  void shouldReturn400_whenCountryIsMissing() throws Exception {
    mockMvc
        .perform(
            post("/distributors").contentType(APPLICATION_JSON).content("{\"name\":\"BANDAI\"}"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance("/distributors"))
        .andExpect(hasTimestamp())
        .andExpect(hasErrors(Map.of("country", "country is required")));
  }

  @Test
  void shouldReturn400_whenCountryIsInvalid() throws Exception {
    mockMvc
        .perform(
            post("/distributors")
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"BANDAI\",\"country\":\"-\"}"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Invalid body"))
        .andExpect(hasStatus(400))
        .andExpect(
            containsDetail("not one of the values accepted for Enum class: [MX, JP, ES, US]"))
        .andExpect(hasInstance("/distributors"))
        .andExpect(hasTimestamp());
  }

  @Test
  void shouldReturn409_whenDistributorAlreadyExists() throws Exception {
    when(service.createDistributor(mockRequest))
        .thenThrow(new DistributorAlreadyExistsException(BANDAI.toString(), JP.toString()));

    mockMvc
        .perform(
            post("/distributors")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(defaultType())
        .andExpect(hasTitle("Distributor already exists"))
        .andExpect(hasStatus(409))
        .andExpect(hasDetail("Distributor already exists: BANDAI - JP"))
        .andExpect(hasInstance("/distributors"))
        .andExpect(hasTimestamp());

    verify(service).createDistributor(mockRequest);
  }

  @Test
  void shouldReturn201_whenDistributorIsValid() throws Exception {
    when(service.createDistributor(mockRequest))
        .thenReturn(
            new DistributorResponse(
                1, "BANDAI", "Tamashii Nations", "JP", "https://tamashiiweb.com/"));

    mockMvc
        .perform(
            post("/distributors")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(hasId(1))
        .andExpect(hasName("BANDAI"))
        .andExpect(hasDescription("Tamashii Nations"))
        .andExpect(jsonPath("$.country").value("JP"))
        .andExpect(jsonPath("$.website").value("https://tamashiiweb.com/"));

    verify(service).createDistributor(mockRequest);
  }

  @Test
  void shouldReturn404_whenDistributorIdDoesNotExist() throws Exception {
    when(service.retrieveDistributor(99L)).thenThrow(new DistributorNotFoundException(99L));

    mockMvc
        .perform(get("/distributors/{id}", "99"))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(defaultType())
        .andExpect(hasTitle("Distributor not found"))
        .andExpect(hasStatus(404))
        .andExpect(hasDetail("Distributor not found"))
        .andExpect(hasInstance("/distributors/99"))
        .andExpect(hasTimestamp());

    verify(service).retrieveDistributor(99L);
  }

  @Test
  void shouldReturn200_whenDistributorIdExists() throws Exception {
    when(service.retrieveDistributor(1L))
        .thenReturn(
            new DistributorResponse(
                1, "DAM", "Distribuidora Animéxico", "MX", "https://animexico-online.com/"));

    mockMvc
        .perform(get("/distributors/{id}", "1"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(hasId(1))
        .andExpect(hasName("DAM"))
        .andExpect(hasDescription("Distribuidora Animéxico"))
        .andExpect(jsonPath("$.country").value("MX"))
        .andExpect(jsonPath("$.website").value("https://animexico-online.com/"));

    verify(service).retrieveDistributor(1L);
  }

  @Test
  void shouldReturn200_whenDistributorsAreAvailable() throws Exception {
    when(service.retrieveDistributors())
        .thenReturn(
            List.of(
                new DistributorResponse(
                    1, "DAM", "Distribuidora Animéxico", "MX", "https://animexico-online.com/")));

    mockMvc
        .perform(get("/distributors"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("DAM"))
        .andExpect(jsonPath("$[0].description").value("Distribuidora Animéxico"))
        .andExpect(jsonPath("$[0].country").value("MX"))
        .andExpect(jsonPath("$[0].website").value("https://animexico-online.com/"));

    verify(service).retrieveDistributors();
  }

  @Test
  void shouldReturn404_whenDistributorIdDoesNotFound() throws Exception {
    when(service.updateDistributor(99L, mockRequest))
        .thenThrow(new DistributorNotFoundException(99L));

    mockMvc
        .perform(
            put("/distributors/{id}", "99")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(defaultType())
        .andExpect(hasTitle("Distributor not found"))
        .andExpect(hasStatus(404))
        .andExpect(hasDetail("Distributor not found"))
        .andExpect(hasInstance("/distributors/99"))
        .andExpect(hasTimestamp());

    verify(service).updateDistributor(99L, mockRequest);
  }

  @Test
  void shouldReturn200_whenDistributorIsUpdated() throws Exception {
    when(service.updateDistributor(1L, mockRequest))
        .thenReturn(
            new DistributorResponse(
                1, "BANDAI", "Tamashii Nations", "JP", "https://tamashiiweb.com/"));

    mockMvc
        .perform(
            put("/distributors/{id}", "1")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(hasId(1))
        .andExpect(hasName("BANDAI"))
        .andExpect(hasDescription("Tamashii Nations"))
        .andExpect(jsonPath("$.country").value("JP"))
        .andExpect(jsonPath("$.website").value("https://tamashiiweb.com/"));

    verify(service).updateDistributor(1L, mockRequest);
  }

  @Test
  void shouldReturn200_whenDistributorWasDeleted() throws Exception {
    mockMvc
        .perform(delete("/distributors/{id}", "1").contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());

    verify(service).removeDistributor(1L);
  }
}
