package com.mesofi.mythclothapi.security.roles.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when attempting to create a role that already exists.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#CONFLICT} API response
 * and provides a specific {@link ErrorCode#ROLE_ALREADY_EXISTS} error code that
 * clients can use to handle duplicate resource creation attempts.
 * </p>
 */
@Getter
public class RoleAlreadyExistsException extends ApiException {

    @Serial
    private static final long serialVersionUID = 7262127210202754700L;

    /**
     * Description of the role that already exists.
     */
    private final String description;

    /**
     * Creates a new exception for an existing role.
     *
     * @param description
     *            description of the duplicated role
     */
    public RoleAlreadyExistsException(String description) {
        super("Role with description '%s' already exists".formatted(description));
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
     * @return {@link ErrorCode#ROLE_ALREADY_EXISTS}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.ROLE_ALREADY_EXISTS;
    }
}
