package com.mesofi.mythclothapi.error;

import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ProblemDetail handleApiExceptions(ApiException ex) {

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

    problem.setTitle(ex.getMessage());
    problem.setDetail(ex.getMessage()); // Optional
    problem.setProperty("errorCode", ex.getErrorCode());

    // Include timestamp
    problem.setProperty("timestamp", Instant.now());

    return problem;
  }

  @ExceptionHandler(DistributorAlreadyExistsException.class)
  public ProblemDetail handleDistributorAlreadyExists(
      DistributorAlreadyExistsException ex, HttpServletRequest request) {

    // Build the URI based on the current server name
    String baseUrl =
        request.getRequestURL().toString().replace(request.getRequestURI(), ""); // remove path

    URI type = URI.create(baseUrl + "/errors/distributor-already-exists");

    ProblemDetail problem = createProblemDetail(ex.getStatus());

    problem.setTitle("Distributor already exists");
    problem.setDetail(ex.getMessage());
    problem.setType(type);
    problem.setProperty("errorCode", ex.getErrorCode());
    problem.setProperty("timestamp", Instant.now());

    // Domain-specific contextual fields
    problem.setProperty("name", ex.getName());
    problem.setProperty("country", ex.getCountry());
    problem.setInstance(URI.create(request.getRequestURI()));

    return problem;
  }

  @ExceptionHandler(DistributorNotFoundException.class)
  public ProblemDetail handleDistributorNotFound(
      DistributorNotFoundException ex, HttpServletRequest request) {

    // Build the URI based on the current server name
    String baseUrl =
        request.getRequestURL().toString().replace(request.getRequestURI(), ""); // remove path

    URI type = URI.create(baseUrl + "/errors/distributor-not-found");

    ProblemDetail problem = createProblemDetail(ex.getStatus());

    problem.setTitle("Distributor not found");
    problem.setDetail(ex.getMessage());
    problem.setType(type);
    problem.setProperty("errorCode", ex.getErrorCode());
    problem.setProperty("timestamp", Instant.now());

    // Domain-specific contextual fields
    // problem.setProperty("name", ex.getName());
    // problem.setProperty("country", ex.getCountry());
    problem.setInstance(URI.create(request.getRequestURI()));

    return problem;
  }

  private ProblemDetail createProblemDetail(HttpStatus status) {
    return ProblemDetail.forStatus(status);
  }
}
