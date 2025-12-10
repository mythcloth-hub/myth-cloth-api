package com.mesofi.mythclothapi.figurines;

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

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.utils.JsonUtils;

@WebMvcTest(FigurineController.class)
public class FigurineControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean FigurineService service;

  @Test
  void shouldReturn400_whenBodyIsMissing() throws Exception {
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
  void shouldReturn415_whenBodyIsText() throws Exception {
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
  void shouldReturn400_whenBodyIsUnparseable() throws Exception {
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

  @Test
  void shouldReturn400_whenBodyIsEmpty() throws Exception {
    String json = JsonUtils.read("payloads/figurines/shouldReturn400_whenBodyIsEmpty.json");

    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(json))
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

  @Test
  void shouldReturn400_whenDistributorIsEmpty() throws Exception {
    String json = JsonUtils.read("payloads/figurines/shouldReturn400_whenDistributorIsEmpty.json");

    mockMvc
        .perform(post("/figurines").contentType(APPLICATION_JSON).content(json))
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
}
