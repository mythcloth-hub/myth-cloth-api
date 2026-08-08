package com.mesofi.mythclothapi.security.permissions.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when attempting to create a permission that already exists.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#CONFLICT} API response
 * and provides a specific {@link ErrorCode#PERMISSION_ALREADY_EXISTS} error
 * code that clients can use to handle duplicate resource creation attempts.
 * </p>
 */
@Getter
public class PermissionAlreadyExistsException extends ApiException {

    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * Description of the permission that already exists.
     */
    private final String description;

    /**
     * Creates a new exception for an existing permission.
     *
     * @param description
     *            description of the duplicated permission
     */
    public PermissionAlreadyExistsException(String description) {
        super("Permission with description '%s' already exists".formatted(description));
        this.description = description;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link HttpStatus#CONFLICT}
     */
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#PERMISSION_ALREADY_EXISTS}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.PERMISSION_ALREADY_EXISTS;
    }
}
