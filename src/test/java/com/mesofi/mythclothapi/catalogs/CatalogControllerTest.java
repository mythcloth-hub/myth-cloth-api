package com.mesofi.mythclothapi.catalogs;

import static com.mesofi.mythclothapi.utils.CommonAssertions.hasDescription;
import static com.mesofi.mythclothapi.utils.CommonAssertions.hasId;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.containsDetail;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.defaultType;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasDetail;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasErrors;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasInstance;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasStatus;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasTimestamp;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasTitle;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogType;
import com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException;

@WebMvcTest(CatalogController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CatalogControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CatalogService service;

  private final String CATALOG = "/catalogs";

  @Test
  void createCatalog_shouldReturn404_whenEndpointsDoNotExist() throws Exception {
    mockMvc
        .perform(post(CATALOG))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(defaultType())
        .andExpect(hasTitle("Endpoint not found"))
        .andExpect(hasStatus(404))
        .andExpect(containsDetail("The URL you are calling does not exist."))
        .andExpect(hasInstance("/catalogs"))
        .andExpect(hasTimestamp());
  }

  @Test
  void createCatalog_shouldReturn400_whenCatalogNotFound() throws Exception {
    mockMvc
        .perform(post(CATALOG + "/unknown"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(containsDetail("Your request parameters didn't convert correctly"))
        .andExpect(hasInstance("/catalogs/unknown"))
        .andExpect(hasTimestamp());
  }

  Stream<Arguments> resourcesProvider() {
    return Arrays.stream(CatalogType.values())
        .map(Enum::name)
        .map(resourceName -> "/" + resourceName)
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void createCatalog_shouldReturn400_whenBodyIsMissing(String resource) throws Exception {
    mockMvc
        .perform(post(CATALOG + resource))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Invalid body"))
        .andExpect(hasStatus(400))
        .andExpect(containsDetail("Required request body is missing"))
        .andExpect(hasInstance(CATALOG + resource))
        .andExpect(hasTimestamp());
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void createCatalog_shouldReturn415_whenBodyIsText(String resource) throws Exception {
    mockMvc
        .perform(post(CATALOG + resource).content("The Body"))
        .andDo(print())
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(defaultType())
        .andExpect(hasTitle("Unsupported Media Type"))
        .andExpect(hasStatus(415))
        .andExpect(hasDetail("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(hasInstance(CATALOG + resource))
        .andExpect(hasTimestamp());
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void createCatalog_shouldReturn400_whenBodyIsUnparseable(String resource) throws Exception {
    mockMvc
        .perform(post(CATALOG + resource).contentType(APPLICATION_JSON).content("The Body"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Invalid body"))
        .andExpect(hasStatus(400))
        .andExpect(
            hasDetail(
                "JSON parse error: Unrecognized token 'The': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')"))
        .andExpect(hasInstance(CATALOG + resource))
        .andExpect(hasTimestamp());
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void createCatalog_shouldReturn400_whenBodyIsEmpty(String resource) throws Exception {
    mockMvc
        .perform(post(CATALOG + resource).contentType(APPLICATION_JSON).content("{}"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance(CATALOG + resource))
        .andExpect(hasTimestamp())
        .andExpect(hasErrors(Map.of("description", "description must not be blank")));
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void createCatalog_shouldReturn400_whenDescriptionIsTooLong(String resource) throws Exception {
    CatalogReq mockRequest = new CatalogReq(("Lorem ").repeat(50));

    mockMvc
        .perform(
            post(CATALOG + resource)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance(CATALOG + resource))
        .andExpect(hasTimestamp())
        .andExpect(hasErrors(Map.of("description", "description must not exceed 100 characters")));
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void createCatalog_shouldReturn201_whenValidInfoIsProvided(String resource) throws Exception {
    CatalogReq mockRequest = new CatalogReq("The description");

    when(service.createCatalog(resource.substring(1), mockRequest))
        .thenReturn(new CatalogResp(1L, "The description"));

    mockMvc
        .perform(
            post(CATALOG + resource)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(
            header().string("Location", String.format("http://localhost/catalogs%s/1", resource)))
        .andExpect(hasId(1))
        .andExpect(hasDescription("The description"));

    verify(service).createCatalog(resource.substring(1), mockRequest);
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void retrieveCatalog_shouldReturn405_whenMethodIsNotSupported(String resource) throws Exception {
    mockMvc
        .perform(get(CATALOG + resource))
        .andDo(print())
        .andExpect(status().isMethodNotAllowed());
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void retrieveCatalog_shouldReturn404_whenCatalogNotFound(String resource) throws Exception {
    when(service.retrieveCatalog(resource.substring(1), 0L))
        .thenThrow(new CatalogNotFoundException(resource));

    mockMvc
        .perform(get(CATALOG + resource + "/0"))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(defaultType())
        .andExpect(hasTitle("Catalog not found: " + resource))
        .andExpect(hasStatus(404))
        .andExpect(containsDetail("Catalog not found: " + resource))
        .andExpect(hasInstance(CATALOG + resource + "/" + 0))
        .andExpect(hasTimestamp());

    verify(service).retrieveCatalog(resource.substring(1), 0L);
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void retrieveCatalog_shouldReturn200_whenCatalogIsFound(String resource) throws Exception {
    when(service.retrieveCatalog(resource.substring(1), 1L))
        .thenReturn(new CatalogResp(1, "The description"));

    mockMvc
        .perform(get(CATALOG + resource + "/1"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(hasId(1))
        .andExpect(hasDescription("The description"));

    verify(service).retrieveCatalog(resource.substring(1), 1L);
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void updateCatalog_shouldReturn405_whenMethodIsNotSupported(String resource) throws Exception {
    mockMvc
        .perform(put(CATALOG + resource))
        .andDo(print())
        .andExpect(status().isMethodNotAllowed());
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void updateCatalog_shouldReturn404_whenCatalogNotFound(String resource) throws Exception {
    CatalogReq mockRequest = new CatalogReq("The description");
    when(service.updateCatalog(resource.substring(1), 0L, new CatalogReq("The description")))
        .thenThrow(new CatalogNotFoundException(resource));

    mockMvc
        .perform(
            put(CATALOG + resource + "/0")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(defaultType())
        .andExpect(hasTitle("Catalog not found: " + resource))
        .andExpect(hasStatus(404))
        .andExpect(containsDetail("Catalog not found: " + resource))
        .andExpect(hasInstance(CATALOG + resource + "/" + 0))
        .andExpect(hasTimestamp());

    verify(service).updateCatalog(resource.substring(1), 0L, new CatalogReq("The description"));
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void updateCatalog_shouldReturn200_whenCatalogIsUpdated(String resource) throws Exception {
    CatalogReq mockRequest = new CatalogReq("Updated Description");
    when(service.updateCatalog(resource.substring(1), 1L, new CatalogReq("Updated Description")))
        .thenReturn(new CatalogResp(1, "Updated Description"));

    mockMvc
        .perform(
            put(CATALOG + resource + "/1")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(hasId(1))
        .andExpect(hasDescription("Updated Description"));

    verify(service).updateCatalog(resource.substring(1), 1L, new CatalogReq("Updated Description"));
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void deleteCatalog_shouldReturn405_whenMethodIsNotSupported(String resource) throws Exception {
    mockMvc
        .perform(delete(CATALOG + resource))
        .andDo(print())
        .andExpect(status().isMethodNotAllowed());
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void deleteCatalog_shouldReturn404_whenCatalogNotFound(String resource) throws Exception {
    doThrow(new CatalogNotFoundException(resource))
        .when(service)
        .deleteCatalog(resource.substring(1), 0L);

    mockMvc
        .perform(delete(CATALOG + resource + "/0").contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(defaultType())
        .andExpect(hasTitle("Catalog not found: " + resource))
        .andExpect(hasStatus(404))
        .andExpect(containsDetail("Catalog not found: " + resource))
        .andExpect(hasInstance(CATALOG + resource + "/" + 0))
        .andExpect(hasTimestamp());

    verify(service).deleteCatalog(resource.substring(1), 0L);
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void deleteCatalog_shouldReturn200_whenCatalogIsDeleted(String resource) throws Exception {
    doNothing().when(service).deleteCatalog(resource.substring(1), 1L);

    mockMvc
        .perform(delete(CATALOG + resource + "/1").contentType(APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());

    verify(service).deleteCatalog(resource.substring(1), 1L);
  }
}
