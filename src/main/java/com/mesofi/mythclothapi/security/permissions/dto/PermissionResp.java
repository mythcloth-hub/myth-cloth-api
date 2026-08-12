package com.mesofi.mythclothapi.security.permissions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response representation of an application permission.
 *
 * <p>
 * Empty properties are excluded from the JSON representation.
 * </p>
 *
 * @param id
 *            the unique identifier of the permission
 * @param description
 *            the description or name of the permission
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PermissionResp(long id, String description) {
}