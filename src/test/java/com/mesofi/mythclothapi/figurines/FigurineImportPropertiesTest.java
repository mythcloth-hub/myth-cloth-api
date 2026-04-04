package com.mesofi.mythclothapi.figurines;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = FigurineConfig.class,
    properties = {
      "myth-cloth.import.drive-url=https://drive.google.com/uc?export=download&id=%s",
      "myth-cloth.import.file-id=catalog-file-id"
    })
class FigurineImportPropertiesTest {

  @Autowired private FigurineImportProperties properties;

  @Test
  void buildUrl_shouldReturnResolvedUrl_whenDriveUrlAndFileIdAreSet() {
    // Arrange
    FigurineImportProperties importProperties = new FigurineImportProperties();
    importProperties.setDriveUrl("https://drive.google.com/uc?export=download&id=%s");
    importProperties.setFileId("my-file-id");

    // Act
    String resolvedUrl = importProperties.buildUrl();

    // Assert
    assertThat(resolvedUrl).isEqualTo("https://drive.google.com/uc?export=download&id=my-file-id");
  }

  @Test
  void buildUrl_shouldThrowException_whenDriveUrlIsNull() {
    // Arrange
    FigurineImportProperties importProperties = new FigurineImportProperties();
    importProperties.setFileId("my-file-id");

    // Act + Assert
    assertThatThrownBy(importProperties::buildUrl).isInstanceOf(NullPointerException.class);
  }

  @Test
  void bindProperties_shouldPopulateFields_whenSpringContextLoads() {
    // Assert
    assertThat(properties.getDriveUrl())
        .isEqualTo("https://drive.google.com/uc?export=download&id=%s");
    assertThat(properties.getFileId()).isEqualTo("catalog-file-id");
  }

  @Test
  void buildUrl_shouldReturnResolvedUrl_whenValuesAreBoundFromSpringProperties() {
    // Act
    String resolvedUrl = properties.buildUrl();

    // Assert
    assertThat(resolvedUrl)
        .isEqualTo("https://drive.google.com/uc?export=download&id=catalog-file-id");
  }
}
