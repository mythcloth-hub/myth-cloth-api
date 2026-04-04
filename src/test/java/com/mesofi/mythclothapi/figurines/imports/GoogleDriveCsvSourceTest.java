package com.mesofi.mythclothapi.figurines.imports;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mesofi.mythclothapi.figurines.FigurineImportProperties;

class GoogleDriveCsvSourceTest {

  @Test
  void openReader_shouldReturnReaderForResolvedUrl() throws IOException {
    // Arrange
    FigurineImportProperties properties = Mockito.mock(FigurineImportProperties.class);
    GoogleDriveCsvSource source = new GoogleDriveCsvSource(properties);
    Path csvPath = Files.createTempFile("google-drive-csv-source", ".csv");
    Files.writeString(csvPath, "id,name\n1,Aiolos", StandardCharsets.UTF_8);
    when(properties.buildUrl()).thenReturn(csvPath.toUri().toString());

    // Act
    try (var reader = source.openReader();
        var bufferedReader = new BufferedReader(reader)) {
      String content = bufferedReader.lines().collect(Collectors.joining("\n"));

      // Assert
      assertThat(content).isEqualTo("id,name\n1,Aiolos");
      verify(properties).buildUrl();
    } finally {
      Files.deleteIfExists(csvPath);
    }
  }

  @Test
  void openReader_shouldPropagateIOException_whenUrlCannotBeOpened() {
    // Arrange
    FigurineImportProperties properties = Mockito.mock(FigurineImportProperties.class);
    GoogleDriveCsvSource source = new GoogleDriveCsvSource(properties);
    when(properties.buildUrl()).thenReturn("file:///path/that/does/not/exist.csv");

    // Act + Assert
    assertThatThrownBy(source::openReader).isInstanceOf(IOException.class);
    verify(properties).buildUrl();
  }
}
