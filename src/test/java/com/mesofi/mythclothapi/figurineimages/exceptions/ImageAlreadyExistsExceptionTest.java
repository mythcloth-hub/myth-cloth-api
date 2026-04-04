package com.mesofi.mythclothapi.figurineimages.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

class ImageAlreadyExistsExceptionTest {

  @Test
  void constructor_shouldSetUriCorrectly_whenCreatedWithGivenUri() {
    // Arrange
    URI uri = URI.create("https://images.example/pegasus.jpg");

    // Act
    ImageAlreadyExistsException exception = new ImageAlreadyExistsException(uri);

    // Assert
    assertThat(exception.getUri()).isEqualTo(uri);
  }

  @Test
  void constructor_shouldSetMessageCorrectly_whenCreated() {
    // Arrange & Act
    ImageAlreadyExistsException exception =
        new ImageAlreadyExistsException(URI.create("https://images.example/pegasus.jpg"));

    // Assert
    assertThat(exception.getMessage()).isEqualTo("Image already exists");
  }

  @Test
  void constructor_shouldSetCauseDetailCorrectly_whenCreated() {
    // Arrange & Act
    ImageAlreadyExistsException exception =
        new ImageAlreadyExistsException(URI.create("https://images.example/pegasus.jpg"));

    // Assert
    assertThat(exception.getCauseDetail()).isEqualTo("Image already exists");
  }

  @Test
  void getStatus_shouldReturnBadRequest_whenCalled() {
    // Arrange
    ImageAlreadyExistsException exception =
        new ImageAlreadyExistsException(URI.create("https://images.example/pegasus.jpg"));

    // Act
    HttpStatus status = exception.getStatus();

    // Assert
    assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void exception_shouldBeInstanceOfApiException_whenCreated() {
    // Arrange & Act
    ImageAlreadyExistsException exception =
        new ImageAlreadyExistsException(URI.create("https://images.example/pegasus.jpg"));

    // Assert
    assertThat(exception).isInstanceOf(ApiException.class);
  }

  @Test
  void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
    // Arrange & Act
    ImageAlreadyExistsException exception =
        new ImageAlreadyExistsException(URI.create("https://images.example/pegasus.jpg"));

    // Assert
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }

  @Test
  void constructor_shouldHandleNullUri_whenCreatedWithNullUri() {
    // Arrange & Act
    ImageAlreadyExistsException exception = new ImageAlreadyExistsException(null);

    // Assert
    assertThat(exception.getUri()).isNull();
  }
}
