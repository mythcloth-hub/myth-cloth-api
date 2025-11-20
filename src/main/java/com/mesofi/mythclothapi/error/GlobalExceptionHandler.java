package com.mesofi.mythclothapi.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorNotFoundException;
import com.mesofi.mythclothapi.references.ReferencePairNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NoResourceFoundException.class)
  public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
    return Problem.of(NOT_FOUND, "Endpoint not found", "The URL you are calling does not exist.");
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
    return Problem.of(BAD_REQUEST, "Invalid body", ex.getMessage());
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ProblemDetail handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
    return Problem.of(UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
    ProblemDetail problemDetail =
        Problem.of(BAD_REQUEST, "Validation Failed", "Your request parameters didn't validate");

    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    problemDetail.setProperty("errors", errors);
    return problemDetail;
  }

  @ExceptionHandler(DistributorAlreadyExistsException.class)
  public ProblemDetail handleDistributorAlreadyExists(DistributorAlreadyExistsException ex) {

    return Problem.of(ex.getStatus(), ex.getMessage(), ex.getCauseDetail());
  }

  @ExceptionHandler(DistributorNotFoundException.class)
  public ProblemDetail handleDistributorNotFound(DistributorNotFoundException ex) {

    return Problem.of(ex.getStatus(), ex.getMessage(), ex.getCauseDetail());
  }

  @ExceptionHandler(ReferencePairNotFoundException.class)
  public ProblemDetail handleReferenceNotFound(ReferencePairNotFoundException ex) {

    return Problem.of(ex.getStatus(), ex.getMessage(), ex.getCauseDetail());
  }
}
