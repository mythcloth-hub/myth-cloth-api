package com.mesofi.mythclothapi.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException;
import com.mesofi.mythclothapi.catalogs.exceptions.RepositoryNotFoundException;
import com.mesofi.mythclothapi.collectors.CollectorInvalidTokenException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorNotFoundException;
import com.mesofi.mythclothapi.integration.ServiceName;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  private enum SampleEnum {
    ALPHA,
    BETA
  }

  @Test
  void handleNoResourceFound_shouldReturnNotFoundProblem() {
    ProblemDetail result = handler.handleNoResourceFound(mock(NoResourceFoundException.class));

    assertThat(result.getStatus()).isEqualTo(404);
    assertThat(result.getTitle()).isEqualTo("Endpoint not found");
    assertThat(result.getDetail()).isEqualTo("The URL you are calling does not exist.");
    assertThat(result.getProperties()).containsKey("timestamp");
  }

  @Test
  void handleHttpMessageNotReadable_shouldReturnInvalidBodyProblem() {
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
    when(ex.getMessage()).thenReturn("Invalid payload");

    ProblemDetail result = handler.handleHttpMessageNotReadable(ex);

    assertThat(result.getStatus()).isEqualTo(400);
    assertThat(result.getTitle()).isEqualTo("Invalid body");
    assertThat(result.getDetail()).isEqualTo("Invalid payload");
  }

  @Test
  void handleHttpMediaTypeNotSupported_shouldReturnUnsupportedMediaTypeProblem() {
    HttpMediaTypeNotSupportedException ex = mock(HttpMediaTypeNotSupportedException.class);
    when(ex.getMessage()).thenReturn("Content-Type not supported");

    ProblemDetail result = handler.handleHttpMediaTypeNotSupported(ex);

    assertThat(result.getStatus()).isEqualTo(415);
    assertThat(result.getTitle()).isEqualTo("Unsupported Media Type");
    assertThat(result.getDetail()).isEqualTo("Content-Type not supported");
  }

  @Test
  void handleEnumConversionError_shouldIncludeAllowedValues_whenRequiredTypeIsEnum() {
    MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
    when(ex.getRequiredType()).thenReturn((Class) SampleEnum.class);
    when(ex.getValue()).thenReturn("GAMMA");

    ProblemDetail result = handler.handleEnumConversionError(ex);

    assertThat(result.getStatus()).isEqualTo(400);
    assertThat(result.getTitle()).isEqualTo("Validation Failed");
    assertThat(result.getDetail()).isEqualTo("Your request parameters didn't convert correctly");
    assertThat(result.getProperties())
        .containsEntry(
            "error",
            "Value 'GAMMA' is not valid, provide one of the following values: [ALPHA, BETA]");
  }

  @Test
  void handleEnumConversionError_shouldNotAddErrorProperty_whenRequiredTypeIsNotEnum() {
    MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
    when(ex.getRequiredType()).thenReturn((Class) String.class);
    when(ex.getValue()).thenReturn("anything");

    ProblemDetail result = handler.handleEnumConversionError(ex);

    assertThat(result.getStatus()).isEqualTo(400);
    assertThat(result.getProperties()).doesNotContainKey("error");
  }

  @Test
  void handleValidationErrors_shouldReturnErrorsMap() {
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    when(ex.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors())
        .thenReturn(
            List.of(
                new FieldError("request", "name", "name must not be blank"),
                new FieldError("request", "countryCode", "countryCode is required")));

    ProblemDetail result = handler.handleValidationErrors(ex);

    assertThat(result.getStatus()).isEqualTo(400);
    assertThat(result.getTitle()).isEqualTo("Validation Failed");
    assertThat(result.getDetail()).isEqualTo("Your request parameters didn't validate");
    assertThat(result.getProperties().get("errors"))
        .isEqualTo(
            Map.of("name", "name must not be blank", "countryCode", "countryCode is required"));
  }

  @Test
  void handleApiExceptions_shouldReturnProblemFromExceptionData() {
    ProblemDetail alreadyExists =
        handler.handleDistributorAlreadyExists(
            new DistributorAlreadyExistsException("BANDAI", "JP"));
    ProblemDetail notFound =
        handler.handleDistributorNotFound(new DistributorNotFoundException(1L));
    ProblemDetail catalogNotFound =
        handler.handleCatalogNotFound(new CatalogNotFoundException("lineup"));
    ProblemDetail repositoryNotFound =
        handler.handleRepositoryNotFound(new RepositoryNotFoundException("series"));
    ProblemDetail invalidToken =
        handler.handleCollectorInvalidToken(new CollectorInvalidTokenException("token invalid"));

    assertThat(alreadyExists.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    assertThat(alreadyExists.getTitle()).isEqualTo("Distributor already exists");

    assertThat(notFound.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(notFound.getTitle()).isEqualTo("Distributor not found");

    assertThat(catalogNotFound.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(catalogNotFound.getTitle()).isEqualTo("Catalog not found: lineup");

    assertThat(repositoryNotFound.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(repositoryNotFound.getTitle()).isEqualTo("Repository not found: series");

    assertThat(invalidToken.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(invalidToken.getTitle()).isEqualTo("token invalid");
  }

  @Test
  void handleIntegrationException_shouldUseExceptionMessageForTitleAndDetail() {
    IntegrationException ex =
        new IntegrationException(
            ServiceName.FACEBOOK, HttpStatus.BAD_GATEWAY, "Facebook gateway error");

    ProblemDetail result = handler.handleIntegrationException(ex);

    assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
    assertThat(result.getTitle()).isEqualTo("Facebook gateway error");
    assertThat(result.getDetail()).isEqualTo("Facebook gateway error");
  }
}
