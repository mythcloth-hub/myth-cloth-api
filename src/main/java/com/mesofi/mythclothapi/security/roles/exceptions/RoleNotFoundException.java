package com.mesofi.mythclothapi.security.roles.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a role cannot be found.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#ROLE_NOT_FOUND} error code that
 * clients can use to handle the error programmatically.
 * </p>
 */
@Getter
public class RoleNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * Identifier of the role that could not be found.
     */
    private final Long id;

    /**
     * Creates a new exception for a missing role.
     *
     * @param id
     *            identifier of the role that was not found
     */
    public RoleNotFoundException(Long id) {
        super("Role with id %s was not found".formatted(id));
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
     *
     * @return {@code Role not found}
     */
    @Override
    public String getTitle() {
        return "Role not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#ROLE_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.ROLE_NOT_FOUND;
    }
}
