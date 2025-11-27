package com.mesofi.mythclothapi.references;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mesofi.mythclothapi.entity.DescriptiveEntity;
import com.mesofi.mythclothapi.references.exceptions.ReferencePairNotFoundException;
import com.mesofi.mythclothapi.references.exceptions.RepositoryNotFoundException;
import com.mesofi.mythclothapi.references.model.ReferencePairRequest;
import com.mesofi.mythclothapi.references.model.ReferencePairResponse;

import jakarta.validation.ConstraintViolationException;

@SpringBootTest
@ActiveProfiles("test")
public class ReferencePairServiceTest {

  @Autowired ReferencePairService service;

  @Test
  void createReference_shouldThrowException_whenRequiredFieldsAreNull() {
    assertThatThrownBy(() -> service.createReference(null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createReference.request")
        .hasMessageContaining("createReference.referenceName")
        .hasMessageContaining("must not be null");
  }

  @Test
  void createReference_shouldThrowException_whenRequestIsNull() {
    assertThatThrownBy(() -> service.createReference("some-reference", null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("createReference.request")
        .hasMessageContaining("must not be null");
  }

  @Test
  void createReference_shouldThrowException_whenRepositoryDoesNotExist() {
    assertThatThrownBy(
            () ->
                service.createReference(
                    "some-reference", new ReferencePairRequest("Some description")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(
            "No enum constant com.mesofi.mythclothapi.references.model.ReferencePairType.some-reference");
  }

  @Test
  void createReference_shouldCreateReference_whenGroupIsProvided() {
    ReferencePairRequest request = new ReferencePairRequest("Some description");

    ReferencePairResponse response = service.createReference("groups", request);
    assertThat(response).isNotNull();
    assertThat(response.id()).isNotZero();
    assertThat(response.description()).isEqualTo("Some description");
  }

  @Test
  void retrieveReference_shouldThrowException_whenRequiredFieldsAreNull() {
    assertThatThrownBy(() -> service.retrieveReference(null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("retrieveReference.id")
        .hasMessageContaining("retrieveReference.referenceName")
        .hasMessageContaining("must not be null");
  }

  @Test
  void retrieveReference_shouldThrowException_whenIdIsNull() {
    assertThatThrownBy(() -> service.retrieveReference("some-reference", null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("retrieveReference.id")
        .hasMessageContaining("must not be null");
  }

  @Test
  void retrieveReference_shouldThrowException_whenRepositoryNotFound() {
    assertThatThrownBy(() -> service.retrieveReference("some-reference", -1L))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessage("Repository not found: some-reference");
  }

  @Test
  void retrieveReference_shouldThrowException_whenIdNotFound() {
    assertThatThrownBy(() -> service.retrieveReference("series", -1L))
        .isInstanceOf(ReferencePairNotFoundException.class)
        .hasMessage("Reference not found: ID -1 not found in reference 'series'");
  }

  @Test
  void retrieveReference_shouldRetrieveReference_whenSeriesIsProvided() {
    ReferencePairRequest request = new ReferencePairRequest("The description");
    ReferencePairResponse resp = service.createReference("series", request);

    ReferencePairResponse response = service.retrieveReference("series", resp.id());
    assertThat(response).isNotNull();
    assertThat(response.id()).isNotZero();
    assertThat(response.description()).isEqualTo("The description");
  }

  @Test
  void retrieveReferenceWithDescription_shouldThrowException_whenRequiredFieldsAreNull() {
    assertThatThrownBy(() -> service.retrieveReferenceWithDescription(null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("retrieveReferenceWithDescription.description")
        .hasMessageContaining("retrieveReferenceWithDescription.referenceName")
        .hasMessageContaining("must not be null");
  }

  @Test
  void retrieveReferenceWithDescription_shouldThrowException_whenIdIsNull() {
    assertThatThrownBy(() -> service.retrieveReferenceWithDescription("some-reference", null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("retrieveReferenceWithDescription.description")
        .hasMessageContaining("must not be null");
  }

  @Test
  void retrieveReferenceWithDescription_shouldThrowException_whenRepositoryNotFound() {
    assertThatThrownBy(
            () -> service.retrieveReferenceWithDescription("some-reference", "The description"))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessage("Repository not found: some-reference");
  }

  @Test
  void retrieveReferenceWithDescription_shouldThrowException_whenIdNotFound_() {
    assertThatThrownBy(() -> service.retrieveReferenceWithDescription("series", "The description"))
        .isInstanceOf(ReferencePairNotFoundException.class)
        .hasMessage(
            "Reference not found: Description 'The description' not found in reference 'series'");
  }

  @Test
  void retrieveReferenceWithDescription_shouldRetrieveReference_whenSeriesIsProvided() {
    ReferencePairRequest request = new ReferencePairRequest("Some description");
    ReferencePairResponse resp = service.createReference("series", request);

    DescriptiveEntity descriptiveEntity =
        service.retrieveReferenceWithDescription("series", resp.description());
    assertThat(descriptiveEntity).isNotNull();
    assertThat(descriptiveEntity.getDescription()).isEqualTo("Some description");
  }

  @Test
  void updateReference_shouldThrowException_whenRequiredFieldsAreNull() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.updateReference(null, null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("updateReference.referenceName")
        .hasMessageContaining("updateReference.id")
        .hasMessageContaining("updateReference.request")
        .hasMessageContaining("must not be null");
  }

  @Test
  void updateReference_shouldThrowException_whenIdAndRequestAreNull() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.updateReference("some-reference", null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("updateReference.id")
        .hasMessageContaining("updateReference.request")
        .hasMessageContaining("must not be null");
  }

  @Test
  void updateReference_shouldThrowException_whenRequestIsNull() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.updateReference("some-reference", -1L, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("updateReference.request")
        .hasMessageContaining("must not be null");
  }

  @Test
  void updateReference_shouldThrowException_whenReferenceNotFound() {
    // Arrange
    ReferencePairRequest request = new ReferencePairRequest("Some description");

    // Act, Assert
    assertThatThrownBy(() -> service.updateReference("series", -1L, request))
        .isInstanceOf(ReferencePairNotFoundException.class)
        .hasMessage("Reference not found: ID -1 not found in reference 'series'");
  }

  @Test
  void updateReference_shouldUpdateReference_whenGroupIsProvided() {
    // Arrange
    ReferencePairRequest request = new ReferencePairRequest("Some description");
    ReferencePairResponse response = service.createReference("groups", request);
    ReferencePairRequest updatedRequest = new ReferencePairRequest("Updated description");

    // Act
    ReferencePairResponse updatedResponse =
        service.updateReference("groups", response.id(), updatedRequest);

    // Assert
    assertThat(updatedResponse).isNotNull();
    assertThat(updatedResponse.id()).isNotZero();
    assertThat(updatedResponse.description()).isEqualTo("Updated description");
  }

  @Test
  void deleteReference_shouldThrowException_whenRequiredFieldsAreNull() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.deleteReference(null, null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("deleteReference.referenceName")
        .hasMessageContaining("deleteReference.id")
        .hasMessageContaining("must not be null");
  }

  @Test
  void deleteReference_shouldThrowException_whenIdIsNull() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.deleteReference("some-reference", null))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("deleteReference.id")
        .hasMessageContaining("must not be null");
  }

  @Test
  void deleteReference_shouldThrowException_whenRepositoryNotFound() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.deleteReference("some-reference", -1L))
        .isInstanceOf(RepositoryNotFoundException.class)
        .hasMessage("Repository not found: some-reference");
  }

  @Test
  void deleteReference_shouldThrowException_whenReferenceNotFound() {
    // Arrange, Act, Assert
    assertThatThrownBy(() -> service.deleteReference("series", -1L))
        .isInstanceOf(ReferencePairNotFoundException.class)
        .hasMessage("Reference not found: ID -1 not found in reference 'series'");
  }

  @Test
  void deleteReference_shouldDeleteReference_whenSeriesIsProvided() {
    // Arrange
    ReferencePairRequest request = new ReferencePairRequest("Some description");
    ReferencePairResponse response = service.createReference("series", request);

    // Act
    service.deleteReference("series", response.id());

    // Assert
    assertThatThrownBy(() -> service.deleteReference("series", response.id()))
        .isInstanceOf(ReferencePairNotFoundException.class);
  }
}
