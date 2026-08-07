package com.mesofi.mythclothapi.security.permissions.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a permission cannot be found.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#PERMISSION_NOT_FOUND} error code
 * that clients can use to handle missing permission resources programmatically.
 * </p>
 */
@Getter
public class PermissionNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * Identifier of the permission that could not be found.
     */
    private final Long id;

    /**
     * Creates a new exception for a missing permission using a custom message.
     *
     * @param message
     *            error detail describing why the permission could not be found
     */
    public PermissionNotFoundException(String message) {
        super(message);
        this.id = null;
    }

    /**
     * Creates a new exception for a missing permission identified by its ID.
     *
     * @param id
     *            identifier of the permission that was not found
     */
    public PermissionNotFoundException(Long id) {
        super("Permission with id %s was not found".formatted(id));
        this.id = id;
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
     */
    @Override
    public String getTitle() {
        return "Permission not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#PERMISSION_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.PERMISSION_NOT_FOUND;
    }
}
