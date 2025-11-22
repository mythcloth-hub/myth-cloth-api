package com.mesofi.mythclothapi.references;

import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.containsDetail;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.defaultType;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasDetail;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasErrors;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasInstance;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasStatus;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasTimestamp;
import static com.mesofi.mythclothapi.utils.ProblemDetailAssertions.hasTitle;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

import com.mesofi.mythclothapi.references.model.ReferencePairType;

@WebMvcTest(ReferencePairController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReferencePairControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ReferencePairService service;

  private final String REF = "/ref";

  @Test
  void shouldReturn404_whenEndpointsDoNotExist() throws Exception {
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
  void shouldReturn400_whenReferenceNotFound() throws Exception {
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
  void shouldReturn400_whenBodyIsMissing(String resource) throws Exception {
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
  void shouldReturn415_whenBodyIsText(String resource) throws Exception {
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
  void shouldReturn400_whenBodyIsUnparseable(String resource) throws Exception {
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
  void shouldReturn400_whenBodyIsEmpty(String resource) throws Exception {
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
}
