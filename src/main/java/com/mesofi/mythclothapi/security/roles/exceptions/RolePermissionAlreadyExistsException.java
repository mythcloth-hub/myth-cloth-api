package com.mesofi.mythclothapi.security.roles.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when attempting to associate a permission with a role that
 * already has that permission assigned.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#CONFLICT} API response
 * and provides a specific {@link ErrorCode#ROLE_PERMISSION_ALREADY_EXISTS}
 * error code that clients can use to handle duplicate role-permission
 * associations programmatically.
 * </p>
 */
@Getter
public class RolePermissionAlreadyExistsException extends ApiException {

    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * Identifier of the role involved in the duplicate association.
     */
    private final Long roleId;

    /**
     * Identifier of the permission already assigned to the role.
     */
    private final Long permissionId;

    /**
     * Creates a new exception for an existing role-permission association.
     *
     * @param roleId
     *            identifier of the role
     * @param permissionId
     *            identifier of the permission already assigned to the role
     */
    public RolePermissionAlreadyExistsException(Long roleId, Long permissionId) {
        super("Role with ID %s already has permission %s assigned.".formatted(roleId, permissionId));
        this.roleId = roleId;
        this.permissionId = permissionId;
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
     * @return {@link ErrorCode#ROLE_PERMISSION_ALREADY_EXISTS}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.ROLE_PERMISSION_ALREADY_EXISTS;
    }
}
