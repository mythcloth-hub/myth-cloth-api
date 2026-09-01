package com.mesofi.mythclothapi.collectors.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a collector provides invalid credentials.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#UNAUTHORIZED} API
 * response and provides a specific
 * {@link ErrorCode#COLLECTOR_INVALID_EMAIL_OR_PASSWORD} error code that clients
 * can use to handle the error programmatically.
 * </p>
 */
@Getter
public class CollectorInvalidCredentialsException extends ApiException {

    @Serial
    private static final long serialVersionUID = 6649970713288460120L;

    /**
     * Creates a new exception for invalid collector credentials.
     */
    public CollectorInvalidCredentialsException() {
        super("Invalid email or password");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link HttpStatus#UNAUTHORIZED}
     */
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Invalid email or password";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#COLLECTOR_INVALID_EMAIL_OR_PASSWORD}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.COLLECTOR_INVALID_EMAIL_OR_PASSWORD;
    }
}
