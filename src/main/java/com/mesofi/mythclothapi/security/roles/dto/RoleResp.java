package com.mesofi.mythclothapi.security.roles.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response representation of an application role.
 *
 * <p>
 * Empty properties are excluded from the JSON representation.
 * </p>
 *
 * @param id
 *            the unique identifier of the role
 * @param description
 *            the description or display name of the role
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RoleResp(long id, String description) {
}
