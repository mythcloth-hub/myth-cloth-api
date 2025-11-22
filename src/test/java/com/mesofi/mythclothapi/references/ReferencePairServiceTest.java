package com.mesofi.mythclothapi.references;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
  void createReference_shouldThrowException_whenGroupRepositoryIsMissing() {
    ReferencePairRequest request = new ReferencePairRequest("Some description");

    ReferencePairResponse response = service.createReference("groups", request);
    assertThat(response).isNotNull();
    assertThat(response.id()).isNotZero();
    assertThat(response.description()).isEqualTo("Some description");
  }
}
