package com.mesofi.mythclothapi.figurineimages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.mesofi.mythclothapi.figurineimages.dto.FigurineImageReq;
import com.mesofi.mythclothapi.figurineimages.dto.FigurineImageResp;
import com.mesofi.mythclothapi.figurineimages.exceptions.ImageAlreadyExistsException;
import com.mesofi.mythclothapi.figurineimages.exceptions.ImageNotFoundException;
import com.mesofi.mythclothapi.figurines.FigurineRepository;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;

@SpringBootTest(classes = {FigurineImageService.class})
public class FigurineImageServiceTest {

  @Autowired private FigurineImageService figurineImageService;

  @MockitoBean private FigurineRepository figurineRepository;

  @Test
  void retrieveFigurineImages_shouldReturnOfficialImages_whenFlagIsTrue() {
    // Arrange
    Figurine figurine =
        figurine(
            1L,
            List.of("https://images.example/official1.jpg", "https://images.example/official2.jpg"),
            List.of("https://images.example/fan1.jpg"));
    when(figurineRepository.findById(1L)).thenReturn(Optional.of(figurine));

    // Act
    FigurineImageResp response = figurineImageService.retrieveFigurineImages(1L, true);

    // Assert
    assertThat(response.officialImageUrls())
        .containsExactly(
            "https://images.example/official1.jpg", "https://images.example/official2.jpg");
    verify(figurineRepository).findById(1L);
  }

  @Test
  void retrieveFigurineImages_shouldReturnNonOfficialImages_whenFlagIsFalse() {
    // Arrange
    Figurine figurine =
        figurine(
            1L,
            List.of("https://images.example/official1.jpg"),
            List.of("https://images.example/fan1.jpg", "https://images.example/fan2.jpg"));
    when(figurineRepository.findById(1L)).thenReturn(Optional.of(figurine));

    // Act
    FigurineImageResp response = figurineImageService.retrieveFigurineImages(1L, false);

    // Assert
    assertThat(response.officialImageUrls())
        .containsExactly("https://images.example/fan1.jpg", "https://images.example/fan2.jpg");
    verify(figurineRepository).findById(1L);
  }

  @Test
  void retrieveFigurineImages_shouldReturnImmutableCopy_whenUnderlyingListIsMutatedAfterCall() {
    // Arrange
    Figurine figurine = figurine(1L, List.of("https://images.example/official1.jpg"), List.of());
    when(figurineRepository.findById(1L)).thenReturn(Optional.of(figurine));

    // Act
    FigurineImageResp response = figurineImageService.retrieveFigurineImages(1L, true);
    figurine.getOfficialImages().add("https://images.example/official2.jpg");

    // Assert
    assertThat(response.officialImageUrls())
        .containsExactly("https://images.example/official1.jpg");
  }

  @Test
  void retrieveFigurineImages_shouldThrowFigurineNotFoundException_whenFigurineDoesNotExist() {
    // Arrange
    when(figurineRepository.findById(10L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> figurineImageService.retrieveFigurineImages(10L, true))
        .isInstanceOfSatisfying(
            FigurineNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Figurine not found");
              assertThat(ex.getId()).isEqualTo(10L);
            });
  }

  @Test
  void removeFigurineImage_shouldRemoveNormalizedOfficialImageAndPersist_whenImageExists() {
    // Arrange
    Figurine figurine =
        figurine(
            1L,
            List.of("https://images.example/official1.jpg", "https://images.example/official2.jpg"),
            List.of("https://images.example/fan1.jpg"));
    URI rawToRemove = URI.create("https://images.example/official/../official2.jpg");

    when(figurineRepository.findById(1L)).thenReturn(Optional.of(figurine));

    // Act
    figurineImageService.removeFigurineImage(1L, rawToRemove, true);

    // Assert
    assertThat(figurine.getOfficialImages())
        .containsExactly("https://images.example/official1.jpg");
    verify(figurineRepository).save(figurine);
  }

  @Test
  void removeFigurineImage_shouldThrowImageNotFoundException_whenImageDoesNotExist() {
    // Arrange
    Figurine figurine = figurine(1L, List.of("https://images.example/official1.jpg"), List.of());
    URI missingImage = URI.create("https://images.example/missing.jpg");

    when(figurineRepository.findById(1L)).thenReturn(Optional.of(figurine));

    // Act + Assert
    assertThatThrownBy(() -> figurineImageService.removeFigurineImage(1L, missingImage, true))
        .isInstanceOfSatisfying(
            ImageNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Image not found");
              assertThat(ex.getUri()).isEqualTo(missingImage);
            });

    verify(figurineRepository, never()).save(any(Figurine.class));
  }

  @Test
  void removeFigurineImage_shouldThrowFigurineNotFoundException_whenFigurineDoesNotExist() {
    // Arrange
    URI image = URI.create("https://images.example/remove.jpg");
    when(figurineRepository.findById(3L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(() -> figurineImageService.removeFigurineImage(3L, image, true))
        .isInstanceOfSatisfying(
            FigurineNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Figurine not found");
              assertThat(ex.getId()).isEqualTo(3L);
            });

    verify(figurineRepository, never()).save(any(Figurine.class));
  }

  @Test
  void createFigurineImage_shouldPersistNormalizedOfficialImage_whenRequestIsValid() {
    // Arrange
    Figurine figurine = figurine(1L, List.of("https://images.example/pegasus.jpg"), List.of());
    URI rawUrl = URI.create("https://images.example/official/../seiya.jpg");
    FigurineImageReq request = request(1L, rawUrl, true);

    when(figurineRepository.findById(1L)).thenReturn(Optional.of(figurine));
    when(figurineRepository.save(any(Figurine.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    FigurineImageResp response = figurineImageService.createFigurineImage(request);

    // Assert
    assertThat(response.officialImageUrls())
        .containsExactly("https://images.example/pegasus.jpg", "https://images.example/seiya.jpg");

    ArgumentCaptor<Figurine> captor = ArgumentCaptor.forClass(Figurine.class);
    verify(figurineRepository).save(captor.capture());
    assertThat(captor.getValue().getOfficialImages())
        .containsExactly("https://images.example/pegasus.jpg", "https://images.example/seiya.jpg");
  }

  @Test
  void createFigurineImage_shouldThrowImageAlreadyExists_whenNormalizedUrlAlreadyExists() {
    // Arrange
    Figurine figurine = figurine(1L, List.of("https://images.example/seiya.jpg"), List.of());
    URI duplicateRaw = URI.create("https://images.example/official/../seiya.jpg");

    when(figurineRepository.findById(1L)).thenReturn(Optional.of(figurine));

    // Act + Assert
    assertThatThrownBy(
            () -> figurineImageService.createFigurineImage(request(1L, duplicateRaw, true)))
        .isInstanceOfSatisfying(
            ImageAlreadyExistsException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Image already exists");
              assertThat(ex.getUri()).isEqualTo(duplicateRaw);
            });

    verify(figurineRepository, never()).save(any(Figurine.class));
  }

  @Test
  void createFigurineImage_shouldThrowFigurineNotFoundException_whenFigurineDoesNotExist() {
    // Arrange
    when(figurineRepository.findById(77L)).thenReturn(Optional.empty());

    // Act + Assert
    assertThatThrownBy(
            () ->
                figurineImageService.createFigurineImage(
                    request(77L, URI.create("https://images.example/a.jpg"), true)))
        .isInstanceOfSatisfying(
            FigurineNotFoundException.class,
            ex -> {
              assertThat(ex.getMessage()).isEqualTo("Figurine not found");
              assertThat(ex.getId()).isEqualTo(77L);
            });

    verify(figurineRepository, never()).save(any(Figurine.class));
  }

  private Figurine figurine(Long id, List<String> officialImages, List<String> nonOfficialImages) {
    Figurine figurine = new Figurine();
    figurine.setId(id);
    figurine.setOfficialImages(new ArrayList<>(officialImages));
    figurine.setNonOfficialImages(new ArrayList<>(nonOfficialImages));
    return figurine;
  }

  private FigurineImageReq request(Long figurineId, URI imageUrl, boolean officialImage) {
    FigurineImageReq request = new FigurineImageReq();
    request.setFigurineId(figurineId);
    request.setImageUrl(imageUrl);
    request.setOfficialImage(officialImage);
    return request;
  }
}
