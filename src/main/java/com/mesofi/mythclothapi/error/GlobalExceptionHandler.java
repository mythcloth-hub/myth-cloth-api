package com.mesofi.mythclothapi.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException;
import com.mesofi.mythclothapi.catalogs.exceptions.CatalogRepositoryNotFoundException;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorInvalidTokenException;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionAlreadyExistsException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionNotFoundException;
import com.mesofi.mythclothapi.collectorspurchases.exceptions.CollectorPurchaseNotFoundException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorAlreadyExistsException;
import com.mesofi.mythclothapi.distributors.exceptions.DistributorNotFoundException;
import com.mesofi.mythclothapi.security.permissions.exceptions.PermissionAlreadyExistsException;
import com.mesofi.mythclothapi.security.permissions.exceptions.PermissionNotFoundException;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleAlreadyExistsException;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleNotFoundException;
import com.mesofi.mythclothapi.security.roles.exceptions.RolePermissionAlreadyExistsException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        return ApiProblemDetail.of(NOT_FOUND, "Endpoint not found", "The URL you are calling does not exist.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ApiProblemDetail.of(BAD_REQUEST, "Invalid body", ex.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return ApiProblemDetail.of(UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return ApiProblemDetail.of(METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage());
    }

    @ExceptionHandler(CollectorPurchaseNotFoundException.class)
    public ProblemDetail handleCollectorPurchaseNotFoundException(CollectorPurchaseNotFoundException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getDetail());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleEnumConversionError(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problemDetail = ApiProblemDetail.of(BAD_REQUEST, "Validation Failed",
                "Your request parameters didn't convert correctly");

        // Check specifically for Enum conversion failure
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";
            List<String> allowedValues = Arrays.stream(ex.getRequiredType().getEnumConstants()).map(Object::toString)
                    .toList();

            String error = String.format("Value '%s' is not valid, provide one of the following values: %s",
                    invalidValue, allowedValues);
            problemDetail.setProperty("error", error);
        }

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ApiProblemDetail.of(BAD_REQUEST, "Validation Failed",
                "Your request parameters didn't validate");

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        return ApiProblemDetail.of(BAD_REQUEST, "Invalid argument", ex.getMessage());
    }

    @ExceptionHandler(DistributorAlreadyExistsException.class)
    public ProblemDetail handleDistributorAlreadyExists(DistributorAlreadyExistsException ex) {

        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getDetail());
    }

    @ExceptionHandler(DistributorNotFoundException.class)
    public ProblemDetail handleDistributorNotFound(DistributorNotFoundException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getDetail());
    }

    @ExceptionHandler(CatalogNotFoundException.class)
    public ProblemDetail handleCatalogNotFound(CatalogNotFoundException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getDetail());
    }

    @ExceptionHandler(CatalogRepositoryNotFoundException.class)
    public ProblemDetail handleRepositoryNotFound(CatalogRepositoryNotFoundException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getDetail());
    }

    @ExceptionHandler(CollectorInvalidTokenException.class)
    public ProblemDetail handleCollectorInvalidToken(CollectorInvalidTokenException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getDetail());
    }

    @ExceptionHandler(CollectorNotFoundException.class)
    public ProblemDetail handleCollectorNotFound(CollectorNotFoundException ex) {
        return ApiProblemDetail.of(ex);
    }

    @ExceptionHandler(CollectorCollectionNotFoundException.class)
    public ProblemDetail handleCollectionNotFoundException(CollectorCollectionNotFoundException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getDetail());
    }

    @ExceptionHandler(CollectorCollectionAlreadyExistsException.class)
    public ProblemDetail handleCollectionAlreadyExistsException(CollectorCollectionAlreadyExistsException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getDetail());
    }

    @ExceptionHandler(IntegrationException.class)
    public ProblemDetail handleIntegrationException(IntegrationException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getMessage());
    }

    @ExceptionHandler(RoleAlreadyExistsException.class)
    public ProblemDetail handleRoleDuplicateException(RoleAlreadyExistsException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getDetail());
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ProblemDetail handleRoleNotFound(RoleNotFoundException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getDetail());
    }

    @ExceptionHandler(PermissionAlreadyExistsException.class)
    public ProblemDetail handlePermissionDuplicateException(PermissionAlreadyExistsException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getMessage());
    }

    @ExceptionHandler(PermissionNotFoundException.class)
    public ProblemDetail handlePermissionNotFound(PermissionNotFoundException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getDetail());
    }

    @ExceptionHandler(RolePermissionAlreadyExistsException.class)
    public ProblemDetail handleRoleAlreadyAssociatedToPermission(RolePermissionAlreadyExistsException ex) {
        return ApiProblemDetail.of(ex.getStatus(), ex.getMessage(), ex.getDetail());
    }
}
