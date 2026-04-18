package com.mesofi.mythclothapi.distributors;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorNotFoundException;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.DistributorName;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(DistributorController.class)
class DistributorControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DistributorService service;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void createDistributor_shouldReturn400_whenRequestBodyIsMissing() throws Exception {

    mockMvc
        .perform(post("/distributors"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Required request body is missing: public org.springframework.http.ResponseEntity<com.mesofi.mythclothapi.distributors.dto.DistributorResp> com.mesofi.mythclothapi.distributors.DistributorController.createDistributor(com.mesofi.mythclothapi.distributors.dto.DistributorReq)"))
        .andExpect(jsonPath("$.instance").value("/distributors"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void createDistributor_shouldReturn415_whenContentTypeIsMissing() throws Exception {

    mockMvc
        .perform(post("/distributors").content("{}"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(
            jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(jsonPath("$.instance").value("/distributors"))
        .andExpect(jsonPath("$.status").value("415"))
        .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void createDistributor_shouldReturn400_whenRequestBodyFailsValidation() throws Exception {

    mockMvc
        .perform(post("/distributors").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/distributors"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.countryCode").value("countryCode is required"))
        .andExpect(jsonPath("$.errors.name").value("name must not be blank"));
  }

  @Test
  void createDistributor_shouldReturn400_whenCountryValueIsInvalid() throws Exception {

    mockMvc
        .perform(
            post("/distributors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"countryCode\":\"-\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "JSON parse error: Cannot deserialize value of type `com.mesofi.mythclothapi.distributors.model.CountryCode` from String \"-\": not one of the values accepted for Enum class: [CN, JP, MX, US, ES]"))
        .andExpect(jsonPath("$.instance").value("/distributors"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void createDistributor_shouldReturn400_whenNameIsBlank() throws Exception {

    mockMvc
        .perform(
            post("/distributors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"country\":\"JP\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/distributors"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.name").value("name must not be blank"));
  }

  @Test
  void createDistributor_shouldReturn400_whenNameIsInvalid() throws Exception {

    mockMvc
        .perform(
            post("/distributors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"country\":\"JP\", \"name\": \"Test\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "JSON parse error: Cannot deserialize value of type `com.mesofi.mythclothapi.distributors.model.DistributorName` from String \"Test\": not one of the values accepted for Enum class: [DAM, BANDAI_CHINA, BLUE_FIN, BANDAI, DS_DISTRIBUTIONS, DTM]"))
        .andExpect(jsonPath("$.instance").value("/distributors"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void createDistributor_shouldReturn201AndLocationHeader() throws Exception {
    DistributorReq request =
        new DistributorReq(DistributorName.BANDAI, CountryCode.JP, "www.google.com");
    DistributorResp response =
        new DistributorResp(1L, "BANDAI", "Tamashii Nations", "JP", "www.google.com");

    when(service.createDistributor(request)).thenReturn(response);

    mockMvc
        .perform(
            post("/distributors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", endsWith("/distributors/1")))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("BANDAI"))
        .andExpect(jsonPath("$.description").value("Tamashii Nations"))
        .andExpect(jsonPath("$.countryCode").value("JP"))
        .andExpect(jsonPath("$.website").value("www.google.com"));

    verify(service).createDistributor(request);
  }

  @Test
  void createDistributor_shouldReturn409WhenDistributorAlreadyExists() throws Exception {
    DistributorReq request =
        new DistributorReq(DistributorName.BANDAI, CountryCode.JP, "www.google.com");

    when(service.createDistributor(request))
        .thenThrow(
            new DistributorAlreadyExistsException(
                request.name().toString(), request.countryCode().toString()));

    mockMvc
        .perform(
            post("/distributors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Distributor already exists: BANDAI - JP"))
        .andExpect(jsonPath("$.instance").value("/distributors"))
        .andExpect(jsonPath("$.status").value("409"))
        .andExpect(jsonPath("$.title").value("Distributor already exists"))
        .andExpect(jsonPath("$.timestamp").exists());

    verify(service).createDistributor(request);
  }

  @Test
  void retrieveDistributor_shouldReturn404WhenDistributorDoesNotExist() throws Exception {

    when(service.retrieveDistributor(0L)).thenThrow(new DistributorNotFoundException(-1L));

    mockMvc
        .perform(get("/distributors/{id}", 0L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Distributor not found"))
        .andExpect(jsonPath("$.instance").value("/distributors/0"))
        .andExpect(jsonPath("$.status").value("404"))
        .andExpect(jsonPath("$.title").value("Distributor not found"))
        .andExpect(jsonPath("$.timestamp").exists());

    verify(service).retrieveDistributor(0L);
  }

  @Test
  void retrieveDistributor_shouldReturn200_whenDistributorExists() throws Exception {
    DistributorResp response = createResponse(5L, DistributorName.BLUE_FIN, CountryCode.US, null);

    when(service.retrieveDistributor(5L)).thenReturn(response);

    mockMvc
        .perform(get("/distributors/{id}", 5L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5L))
        .andExpect(jsonPath("$.name").value(DistributorName.BLUE_FIN.toString()))
        .andExpect(jsonPath("$.countryCode").value(CountryCode.US.toString()));

    verify(service).retrieveDistributor(5L);
  }

  @Test
  void retrieveDistributors_shouldReturnList_whenDistributorsExist() throws Exception {
    when(service.retrieveDistributors())
        .thenReturn(
            List.of(
                createResponse(1L, DistributorName.BANDAI, CountryCode.JP, null),
                createResponse(2L, DistributorName.DAM, CountryCode.MX, "https://dam.com")));

    mockMvc
        .perform(get("/distributors"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].name").value(DistributorName.BANDAI.toString()))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].website").value("https://dam.com"));

    verify(service).retrieveDistributors();
  }

  @Test
  void retrieveDistributors_shouldReturnEmptyList_whenNoDistributorsExist() throws Exception {
    when(service.retrieveDistributors()).thenReturn(List.of());

    mockMvc
        .perform(get("/distributors"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    verify(service).retrieveDistributors();
  }

  @Test
  void updateDistributor_shouldReturn200_whenRequestIsValid() throws Exception {
    DistributorReq request =
        new DistributorReq(DistributorName.BANDAI, CountryCode.JP, "https://bandai.com");
    DistributorResp response =
        createResponse(3L, DistributorName.BANDAI, CountryCode.JP, "https://bandai.com");

    when(service.updateDistributor(eq(3L), any())).thenReturn(response);

    mockMvc
        .perform(
            put("/distributors/{id}", 3L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(3L))
        .andExpect(jsonPath("$.name").value(DistributorName.BANDAI.toString()))
        .andExpect(jsonPath("$.website").value("https://bandai.com"));

    verify(service).updateDistributor(eq(3L), any());
  }

  @Test
  void updateDistributor_shouldReturn400_whenRequestBodyFailsValidation() throws Exception {

    mockMvc
        .perform(
            put("/distributors/{id}", 1L).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.errors.name").value("name must not be blank"))
        .andExpect(jsonPath("$.errors.countryCode").value("countryCode is required"));

    verifyNoInteractions(service);
  }

  @Test
  void removeDistributor_shouldReturn204_whenDistributorExists() throws Exception {
    mockMvc.perform(delete("/distributors/{id}", 1L)).andExpect(status().isNoContent());

    verify(service).removeDistributor(1L);
  }

  private DistributorResp createResponse(
      long id, DistributorName name, CountryCode country, String website) {
    return new DistributorResp(
        id, name.toString(), name.getDescription(), country.toString(), website);
  }
}
