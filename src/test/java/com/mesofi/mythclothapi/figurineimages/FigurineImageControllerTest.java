package com.mesofi.mythclothapi.figurineimages;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.figurineimages.dto.FigurineImageReq;
import com.mesofi.mythclothapi.figurineimages.dto.FigurineImageResp;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(FigurineImageController.class)
class FigurineImageControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FigurineImageService service;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void createImage_shouldReturn404_whenPostingToRootPath() throws Exception {

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
  void createImage_shouldReturn400_whenRequestBodyIsMissing() throws Exception {

    mockMvc
        .perform(post("/figurines/{figurineId}/images", 1L))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Required request body is missing: public org.springframework.http.ResponseEntity<com.mesofi.mythclothapi.figurineimages.dto.FigurineImageResp> com.mesofi.mythclothapi.figurineimages.FigurineImageController.createImage(java.lang.Long,com.mesofi.mythclothapi.figurineimages.dto.FigurineImageReq)"))
        .andExpect(jsonPath("$.instance").value("/figurines/1/images"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Invalid body"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createImage_shouldReturn415_whenContentTypeIsMissing() throws Exception {

    mockMvc
        .perform(post("/figurines/{figurineId}/images", 1L).content("{}"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(
            jsonPath("$.detail").value("Content-Type 'application/octet-stream' is not supported"))
        .andExpect(jsonPath("$.instance").value("/figurines/1/images"))
        .andExpect(jsonPath("$.status").value("415"))
        .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
        .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(service);
  }

  @Test
  void createImage_shouldReturn400_whenRequestBodyFailsValidation() throws Exception {

    mockMvc
        .perform(
            post("/figurines/{figurineId}/images", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Your request parameters didn't validate"))
        .andExpect(jsonPath("$.instance").value("/figurines/1/images"))
        .andExpect(jsonPath("$.status").value("400"))
        .andExpect(jsonPath("$.title").value("Validation Failed"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.errors.figurineId").value("must not be null"))
        .andExpect(jsonPath("$.errors.imageUrl").value("imageUrl must not be blank"));

    verifyNoInteractions(service);
  }

  @Test
  void createImage_shouldReturn201AndUsePathFigurineId_whenRequestIsValid() throws Exception {
    FigurineImageReq request = new FigurineImageReq();
    request.setFigurineId(999L);
    request.setImageUrl(URI.create("https://images.example/pegasus.jpg"));
    request.setOfficialImage(true);

    FigurineImageResp response =
        new FigurineImageResp(List.of("https://images.example/pegasus.jpg"));
    when(service.createFigurineImage(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/figurines/{figurineId}/images", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.officialImageUrls.length()").value(1))
        .andExpect(jsonPath("$.officialImageUrls[0]").value("https://images.example/pegasus.jpg"));

    ArgumentCaptor<FigurineImageReq> captor = ArgumentCaptor.forClass(FigurineImageReq.class);
    verify(service).createFigurineImage(captor.capture());

    assertThat(captor.getValue().getFigurineId()).isEqualTo(1L);
    assertThat(captor.getValue().getImageUrl())
        .isEqualTo(URI.create("https://images.example/pegasus.jpg"));
    assertThat(captor.getValue().isOfficialImage()).isTrue();
  }

  @Test
  void createImage_shouldDelegateToServiceAndReturnCreated_whenInvokedDirectly() {
    FigurineImageController controller = new FigurineImageController(service);

    FigurineImageReq request = new FigurineImageReq();
    request.setFigurineId(999L);
    request.setImageUrl(URI.create("https://images.example/pegasus.jpg"));
    request.setOfficialImage(true);

    FigurineImageResp response =
        new FigurineImageResp(List.of("https://images.example/pegasus.jpg"));
    when(service.createFigurineImage(request)).thenReturn(response);

    var result = controller.createImage(1L, request);

    assertThat(request.getFigurineId()).isEqualTo(1L);
    assertThat(result.getStatusCode().value()).isEqualTo(201);
    assertThat(result.getBody()).isEqualTo(response);
    verify(service).createFigurineImage(request);
  }

  @Test
  void retrieveImages_shouldReturn200AndUseDefaultOfficialFlag_whenParamIsMissing()
      throws Exception {
    FigurineImageResp response =
        new FigurineImageResp(List.of("https://images.example/official.jpg"));
    when(service.retrieveFigurineImages(1L, true)).thenReturn(response);

    mockMvc
        .perform(get("/figurines/{figurineId}/images", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.officialImageUrls.length()").value(1))
        .andExpect(jsonPath("$.officialImageUrls[0]").value("https://images.example/official.jpg"));

    verify(service).retrieveFigurineImages(1L, true);
  }

  @Test
  void retrieveImages_shouldReturn200_whenOfficialFlagIsFalse() throws Exception {
    FigurineImageResp response = new FigurineImageResp(List.of("https://images.example/fan.jpg"));
    when(service.retrieveFigurineImages(1L, false)).thenReturn(response);

    mockMvc
        .perform(get("/figurines/{figurineId}/images", 1L).param("isOfficialImage", "false"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.officialImageUrls.length()").value(1))
        .andExpect(jsonPath("$.officialImageUrls[0]").value("https://images.example/fan.jpg"));

    verify(service).retrieveFigurineImages(1L, false);
  }

  @Test
  void retrieveImages_shouldReturnBadRequest_whenFigurineIdIsNotPositive() {
    assertThatThrownBy(() -> mockMvc.perform(get("/figurines/{figurineId}/images", 0L)))
        .hasRootCauseInstanceOf(jakarta.validation.ConstraintViolationException.class);
  }

  @Test
  void removeImage_shouldReturn204_whenRequestIsValid() throws Exception {
    mockMvc
        .perform(
            delete("/figurines/{figurineId}/images", 1L)
                .param("imageUrl", "https://images.example/pegasus.jpg")
                .param("isOfficialImage", "false"))
        .andExpect(status().isNoContent());

    verify(service)
        .removeFigurineImage(1L, URI.create("https://images.example/pegasus.jpg"), false);
  }
}
