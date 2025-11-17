package com.mesofi.mythclothapi.distributors;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.model.DistributorRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DistributorController.class)
public class DistributorControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean DistributorService service;

  private final DistributorRequest mockRequest =
      new DistributorRequest("BigBangToys", "US", "url.com");

  @Test
  void shouldReturn409_whenDistributorAlreadyExists() throws Exception {
    when(service.createDistributor(mockRequest))
        .thenThrow(new DistributorAlreadyExistsException("BigBangToys", "US"));

    mockMvc
        .perform(
            post("/distributors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
        .andExpect(status().isConflict());
  }
}
