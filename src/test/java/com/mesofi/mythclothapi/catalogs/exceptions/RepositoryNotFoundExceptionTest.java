package com.mesofi.mythclothapi.catalogs.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

class RepositoryNotFoundExceptionTest {

  @Test
  void constructor_shouldSetNameCorrectly_whenCreatedWithGivenName() {
    // Arrange
    String name = "myth-cloth-repo";

    // Act
    RepositoryNotFoundException exception = new RepositoryNotFoundException(name);

    // Assert
    assertThat(exception.getName()).isEqualTo(name);
  }

  @Test
  void constructor_shouldSetMessageCorrectly_whenCreated() {
    // Arrange & Act
    RepositoryNotFoundException exception = new RepositoryNotFoundException("myth-cloth-repo");

    // Assert
    assertThat(exception.getMessage()).isEqualTo("Repository not found: myth-cloth-repo");
  }

  @Test
  void constructor_shouldSetCauseDetailCorrectly_whenCreated() {
    // Arrange & Act
    RepositoryNotFoundException exception = new RepositoryNotFoundException("myth-cloth-repo");

    // Assert
    assertThat(exception.getCauseDetail()).isEqualTo("Repository not found: myth-cloth-repo");
  }

  @Test
  void getStatus_shouldReturnNotFound_whenCalled() {
    // Arrange
    RepositoryNotFoundException exception = new RepositoryNotFoundException("myth-cloth-repo");

    // Act
    HttpStatus status = exception.getStatus();

    // Assert
    assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void exception_shouldBeInstanceOfApiException_whenCreated() {
    // Arrange & Act
    RepositoryNotFoundException exception = new RepositoryNotFoundException("myth-cloth-repo");

    // Assert
    assertThat(exception).isInstanceOf(ApiException.class);
  }

  @Test
  void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
    // Arrange & Act
    RepositoryNotFoundException exception = new RepositoryNotFoundException("myth-cloth-repo");

    // Assert
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }

  @Test
  void constructor_shouldHandleNullName_whenCreatedWithNullName() {
    // Arrange & Act
    RepositoryNotFoundException exception = new RepositoryNotFoundException(null);

    // Assert
    assertThat(exception.getName()).isNull();
    assertThat(exception.getMessage()).isEqualTo("Repository not found: null");
    assertThat(exception.getCauseDetail()).isEqualTo("Repository not found: null");
  }
}
