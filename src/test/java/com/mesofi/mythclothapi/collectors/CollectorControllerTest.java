package com.mesofi.mythclothapi.collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.collectors.dto.CollectorLoginReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginResp;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CollectorController.class)
class CollectorControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CollectorService service;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void login_shouldReturn400_whenRequestBodyIsMissing() throws Exception {

    mockMvc
        .perform(post("/collectors/auth/{provider}", "google"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail", containsString("Required request body is missing")))
        .andExpect(jsonPath("$.instance").value("/collectors/auth/google"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void login_shouldReturn415_whenContentTypeIsMissing() throws Exception {

    mockMvc
        .perform(post("/collectors/auth/{provider}", "google").content("{}"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(
            jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(jsonPath("$.instance").value("/collectors/auth/google"))
        .andExpect(jsonPath("$.status").value("415"))
        .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void login_shouldReturn200_whenRequestIsValid() throws Exception {
    CollectorLoginReq request = new CollectorLoginReq("google-id-token", null);
    CollectorLoginResp response =
        new CollectorLoginResp(
            1L, "Pegasus Seiya", "seiya@example.com", "api-jwt-token", "Bearer", 3600L);

    when(service.login("google", request)).thenReturn(response);

    mockMvc
        .perform(
            post("/collectors/auth/{provider}", "google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.collectorId").value(1L))
        .andExpect(jsonPath("$.displayName").value("Pegasus Seiya"))
        .andExpect(jsonPath("$.email").value("seiya@example.com"))
        .andExpect(jsonPath("$.accessToken").value("api-jwt-token"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.expiresInSeconds").value(3600));

    verify(service).login("google", request);
  }

  @Test
  void login_shouldDelegateToService_whenInvokedDirectly() {
    CollectorController controller = new CollectorController(service);
    CollectorLoginReq request = new CollectorLoginReq(null, "facebook-access-token");
    CollectorLoginResp response =
        new CollectorLoginResp(2L, "Andromeda Shun", "shun@example.com", "jwt", "Bearer", 1200L);

    when(service.login("facebook", request)).thenReturn(response);

    CollectorLoginResp result = controller.login("facebook", request);

    assertThat(result).isEqualTo(response);
    verify(service).login("facebook", request);
  }
}
