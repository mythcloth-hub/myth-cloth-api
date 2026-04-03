package com.mesofi.mythclothapi.figurineimages.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

class ImageNotFoundExceptionTest {

  @Test
  void constructor_shouldSetUriCorrectly_whenCreatedWithGivenUri() {
    // Arrange
    URI uri = URI.create("https://images.example/pegasus.jpg");

    // Act
    ImageNotFoundException exception = new ImageNotFoundException(uri);

    // Assert
    assertThat(exception.getUri()).isEqualTo(uri);
  }

  @Test
  void constructor_shouldSetMessageCorrectly_whenCreated() {
    // Arrange & Act
    ImageNotFoundException exception =
        new ImageNotFoundException(URI.create("https://images.example/pegasus.jpg"));

    // Assert
    assertThat(exception.getMessage()).isEqualTo("Image not found");
  }

  @Test
  void constructor_shouldSetCauseDetailCorrectly_whenCreated() {
    // Arrange & Act
    ImageNotFoundException exception =
        new ImageNotFoundException(URI.create("https://images.example/pegasus.jpg"));

    // Assert
    assertThat(exception.getCauseDetail()).isEqualTo("Image not found");
  }

  @Test
  void getStatus_shouldReturnNotFound_whenCalled() {
    // Arrange
    ImageNotFoundException exception =
        new ImageNotFoundException(URI.create("https://images.example/pegasus.jpg"));

    // Act
    HttpStatus status = exception.getStatus();

    // Assert
    assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void exception_shouldBeInstanceOfApiException_whenCreated() {
    // Arrange & Act
    ImageNotFoundException exception =
        new ImageNotFoundException(URI.create("https://images.example/pegasus.jpg"));

    // Assert
    assertThat(exception).isInstanceOf(ApiException.class);
  }

  @Test
  void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
    // Arrange & Act
    ImageNotFoundException exception =
        new ImageNotFoundException(URI.create("https://images.example/pegasus.jpg"));

    // Assert
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }

  @Test
  void constructor_shouldHandleNullUri_whenCreatedWithNullUri() {
    // Arrange & Act
    ImageNotFoundException exception = new ImageNotFoundException(null);

    // Assert
    assertThat(exception.getUri()).isNull();
  }
}
