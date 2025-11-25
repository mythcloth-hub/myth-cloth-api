package com.mesofi.mythclothapi.references;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
import com.mesofi.mythclothapi.references.exceptions.ReferencePairNotFoundException;
import com.mesofi.mythclothapi.references.model.ReferencePairRequest;
import com.mesofi.mythclothapi.references.model.ReferencePairResponse;
import com.mesofi.mythclothapi.references.model.ReferencePairType;

@WebMvcTest(ReferencePairController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReferencePairControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ReferencePairService service;

  private final String REF = "/ref";

  @Test
  void createReference_shouldReturn404_whenEndpointsDoNotExist() throws Exception {
    mockMvc
        .perform(post(REF))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(defaultType())
        .andExpect(hasTitle("Endpoint not found"))
        .andExpect(hasStatus(404))
        .andExpect(containsDetail("The URL you are calling does not exist."))
        .andExpect(hasInstance("/ref"))
        .andExpect(hasTimestamp());
  }

  @Test
  void createReference_shouldReturn400_whenReferenceNotFound() throws Exception {
    mockMvc
        .perform(post(REF + "/unknown"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(containsDetail("Your request parameters didn't convert correctly"))
        .andExpect(hasInstance("/ref/unknown"))
        .andExpect(hasTimestamp());
  }

  Stream<Arguments> resourcesProvider() {
    return Arrays.stream(ReferencePairType.values())
        .map(Enum::name)
        .map(resourceName -> "/" + resourceName)
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void createReference_shouldReturn400_whenBodyIsMissing(String resource) throws Exception {
    mockMvc
        .perform(post(REF + resource))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Invalid body"))
        .andExpect(hasStatus(400))
        .andExpect(containsDetail("Required request body is missing"))
        .andExpect(hasInstance(REF + resource))
        .andExpect(hasTimestamp());
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void createReference_shouldReturn415_whenBodyIsText(String resource) throws Exception {
    mockMvc
        .perform(post(REF + resource).content("The Body"))
        .andDo(print())
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(defaultType())
        .andExpect(hasTitle("Unsupported Media Type"))
        .andExpect(hasStatus(415))
        .andExpect(hasDetail("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(hasInstance(REF + resource))
        .andExpect(hasTimestamp());
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void createReference_shouldReturn400_whenBodyIsUnparseable(String resource) throws Exception {
    mockMvc
        .perform(post(REF + resource).contentType(APPLICATION_JSON).content("The Body"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Invalid body"))
        .andExpect(hasStatus(400))
        .andExpect(
            hasDetail(
                "JSON parse error: Unrecognized token 'The': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')"))
        .andExpect(hasInstance(REF + resource))
        .andExpect(hasTimestamp());
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void createReference_shouldReturn400_whenBodyIsEmpty(String resource) throws Exception {
    mockMvc
        .perform(post(REF + resource).contentType(APPLICATION_JSON).content("{}"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance(REF + resource))
        .andExpect(hasTimestamp())
        .andExpect(hasErrors(Map.of("description", "description must not be blank")));
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void createReference_shouldReturn400_whenDescriptionIsTooLong(String resource) throws Exception {
    ReferencePairRequest mockRequest = new ReferencePairRequest(("Lorem ").repeat(50));

    mockMvc
        .perform(
            post(REF + resource)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(defaultType())
        .andExpect(hasTitle("Validation Failed"))
        .andExpect(hasStatus(400))
        .andExpect(hasDetail("Your request parameters didn't validate"))
        .andExpect(hasInstance(REF + resource))
        .andExpect(hasTimestamp())
        .andExpect(hasErrors(Map.of("description", "description must not exceed 100 characters")));
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void createReference_shouldReturn201_whenValidInfoIsProvided(String resource) throws Exception {
    ReferencePairRequest mockRequest = new ReferencePairRequest("The description");

    when(service.createReference(resource.substring(1), mockRequest))
        .thenReturn(new ReferencePairResponse(1L, "The description"));

    mockMvc
        .perform(
            post(REF + resource)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(header().string("Location", String.format("http://localhost/ref%s/1", resource)))
        .andExpect(hasId(1))
        .andExpect(hasDescription("The description"));

    verify(service).createReference(resource.substring(1), mockRequest);
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void retrieveReference_shouldReturn405_whenMethodIsNotSupported(String resource)
      throws Exception {
    mockMvc.perform(get(REF + resource)).andDo(print()).andExpect(status().isMethodNotAllowed());
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void retrieveReference_shouldReturn404_whenReferenceNotFound(String resource) throws Exception {
    when(service.retrieveReference(resource.substring(1), 0L))
        .thenThrow(new ReferencePairNotFoundException(resource));

    mockMvc
        .perform(get(REF + resource + "/0"))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(defaultType())
        .andExpect(hasTitle("Reference not found: " + resource))
        .andExpect(hasStatus(404))
        .andExpect(containsDetail("Reference not found: " + resource))
        .andExpect(hasInstance(REF + resource + "/" + 0))
        .andExpect(hasTimestamp());

    verify(service).retrieveReference(resource.substring(1), 0L);
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void retrieveReference_shouldReturn200_whenReferenceIsFound(String resource) throws Exception {
    when(service.retrieveReference(resource.substring(1), 1L))
        .thenReturn(new ReferencePairResponse(1, "The description"));

    mockMvc
        .perform(get(REF + resource + "/1"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(hasId(1))
        .andExpect(hasDescription("The description"));

    verify(service).retrieveReference(resource.substring(1), 1L);
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void updateReference_shouldReturn405_whenMethodIsNotSupported(String resource) throws Exception {
    mockMvc.perform(put(REF + resource)).andDo(print()).andExpect(status().isMethodNotAllowed());
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void updateReference_shouldReturn404_whenReferenceNotFound(String resource) throws Exception {
    ReferencePairRequest mockRequest = new ReferencePairRequest("The description");
    when(service.updateReference(
            resource.substring(1), 0L, new ReferencePairRequest("The description")))
        .thenThrow(new ReferencePairNotFoundException(resource));

    mockMvc
        .perform(
            put(REF + resource + "/0")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(defaultType())
        .andExpect(hasTitle("Reference not found: " + resource))
        .andExpect(hasStatus(404))
        .andExpect(containsDetail("Reference not found: " + resource))
        .andExpect(hasInstance(REF + resource + "/" + 0))
        .andExpect(hasTimestamp());

    verify(service)
        .updateReference(resource.substring(1), 0L, new ReferencePairRequest("The description"));
  }

  @ParameterizedTest
  @MethodSource("resourcesProvider")
  void updateReference_shouldReturn404_whenReferenceNotFound_(String resource) throws Exception {
    ReferencePairRequest mockRequest = new ReferencePairRequest("Updated Description");
    when(service.updateReference(
            resource.substring(1), 1L, new ReferencePairRequest("Updated Description")))
        .thenReturn(new ReferencePairResponse(1, "Updated Description"));

    mockMvc
        .perform(
            put(REF + resource + "/1")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mockRequest)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(hasId(1))
        .andExpect(hasDescription("Updated Description"));

    verify(service)
        .updateReference(
            resource.substring(1), 1L, new ReferencePairRequest("Updated Description"));
  }
}
