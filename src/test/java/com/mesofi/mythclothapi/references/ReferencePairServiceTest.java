package com.mesofi.mythclothapi.references;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
  void retrieveReference_shouldCreateReference_whenSeriesIsProvided() {
    ReferencePairRequest request = new ReferencePairRequest("Some description");
    ReferencePairResponse resp = service.createReference("series", request);

    ReferencePairResponse response = service.retrieveReference("series", resp.id());
    assertThat(response).isNotNull();
    assertThat(response.id()).isNotZero();
    assertThat(response.description()).isEqualTo("Some description");
  }
}
