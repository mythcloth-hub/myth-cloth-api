package com.mesofi.mythclothapi.error.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

/**
 * Exception thrown when a requested resource is not found.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#RESOURCE_NOT_FOUND} error code that
 * clients can use to handle missing resources.
 * </p>
 */
public class ResourceNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = 1262127210202754700L;

    /**
     * Creates a new exception for a resource that could not be found.
     */
    public ResourceNotFoundException() {
        super("Endpoint not found", "The URL you are calling does not exist.");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link HttpStatus#NOT_FOUND}
     */
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#RESOURCE_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.RESOURCE_NOT_FOUND;
    }
}
