package com.mesofi.mythclothapi.anniversaries;

import static org.mockito.ArgumentMatchers.any;
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

import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryReq;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AnniversaryController.class)
class AnniversaryControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AnniversaryService service;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void createAnniversary_shouldReturn404_whenPostingToRootPath() throws Exception {

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
  void createAnniversary_shouldReturn400_whenRequestBodyIsMissing() throws Exception {

    mockMvc
        .perform(post("/anniversaries"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Required request body is missing: public org.springframework.http.ResponseEntity"
                        + "<com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp>"
                        + " com.mesofi.mythclothapi.anniversaries.AnniversaryController"
                        + ".createAnniversary(com.mesofi.mythclothapi.anniversaries.dto.AnniversaryReq)"))
        .andExpect(jsonPath("$.instance").value("/anniversaries"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createAnniversary_shouldReturn415_whenContentTypeIsMissing() throws Exception {

    mockMvc
        .perform(post("/anniversaries").content("{}"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(
            jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(jsonPath("$.instance").value("/anniversaries"))
        .andExpect(jsonPath("$.status").value("415"))
        .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createAnniversary_shouldReturn400_whenDescriptionExceedsMaxLength() throws Exception {
    String requestBody = "{\"description\":\"%s\", \"year\":30}".formatted("a".repeat(101));

    mockMvc
        .perform(
            post("/anniversaries").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/anniversaries"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(
            jsonPath("$.errors.description").value("description must not exceed 100 characters"));

    verifyNoInteractions(service);
  }

  @Test
  void createAnniversary_shouldReturn201AndLocationHeader() throws Exception {
    AnniversaryReq request = new AnniversaryReq("Saint Seiya 35th Anniversary", 35);
    AnniversaryResp response = new AnniversaryResp(1L, "Saint Seiya 35th Anniversary", 35);

    when(service.createAnniversary(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/anniversaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/anniversaries/1"))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.description").value("Saint Seiya 35th Anniversary"))
        .andExpect(jsonPath("$.year").value(35));

    verify(service).createAnniversary(any());
  }

  @Test
  void retrieveAnniversary_shouldReturn200_whenAnniversaryExists() throws Exception {
    AnniversaryResp response = new AnniversaryResp(1L, "Saint Seiya 35th Anniversary", 35);

    when(service.retrieveAnniversary(1L)).thenReturn(response);

    mockMvc
        .perform(get("/anniversaries/{id}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.description").value("Saint Seiya 35th Anniversary"))
        .andExpect(jsonPath("$.year").value(35));

    verify(service).retrieveAnniversary(1L);
  }

  @Test
  void retrieveAnniversaries_shouldReturnList_whenAnniversariesExist() throws Exception {
    AnniversaryResp first = new AnniversaryResp(1L, "Saint Seiya 35th Anniversary", 35);
    AnniversaryResp second = new AnniversaryResp(2L, "Hades Chapter Anniversary", 20);

    when(service.retrieveAnniversaries()).thenReturn(List.of(first, second));

    mockMvc
        .perform(get("/anniversaries"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].description").value("Saint Seiya 35th Anniversary"))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].description").value("Hades Chapter Anniversary"));

    verify(service).retrieveAnniversaries();
  }

  @Test
  void updateAnniversary_shouldReturn200_whenRequestIsValid() throws Exception {
    AnniversaryReq request = new AnniversaryReq("Updated Anniversary", 40);
    AnniversaryResp response = new AnniversaryResp(1L, "Updated Anniversary", 40);

    when(service.updateAnniversary(1L, request)).thenReturn(response);

    mockMvc
        .perform(
            put("/anniversaries/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.description").value("Updated Anniversary"))
        .andExpect(jsonPath("$.year").value(40));

    verify(service).updateAnniversary(1L, request);
  }

  @Test
  void removeAnniversary_shouldReturn204_whenAnniversaryExists() throws Exception {

    mockMvc.perform(delete("/anniversaries/{id}", 1L)).andExpect(status().isNoContent());

    verify(service).removeAnniversary(1L);
  }
}
