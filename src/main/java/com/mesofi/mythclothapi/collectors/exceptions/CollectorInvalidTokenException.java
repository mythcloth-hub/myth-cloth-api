package com.mesofi.mythclothapi.collectors.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a collector authentication token fails validation.
 *
 * <p>
 * This exception is used when a token provided by an external authentication
 * provider cannot be validated or is considered invalid. It is translated into
 * an {@link HttpStatus#UNAUTHORIZED} API response and provides a specific
 * {@link ErrorCode#INVALID_TOKEN} error code that clients can use to handle
 * authentication failures programmatically.
 * </p>
 */
@Getter
public class CollectorInvalidTokenException extends ApiException {

    @Serial
    private static final long serialVersionUID = -3873142486893506647L;

    /**
     * Creates a new invalid token exception.
     *
     * @param message
     *            validation failure detail provided by the authentication provider
     */
    public CollectorInvalidTokenException(String message) {
        super(message);
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
     *
     * @return {@code Invalid token}
     */
    @Override
    public String getTitle() {
        return "Invalid token";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#INVALID_TOKEN}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.INVALID_TOKEN;
    }
}
