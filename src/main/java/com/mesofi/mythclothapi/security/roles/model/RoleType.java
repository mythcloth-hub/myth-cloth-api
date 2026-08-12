package com.mesofi.mythclothapi.security.roles.model;

import lombok.Getter;

/**
 * Defines the roles available within the application.
 *
 * <p>
 * Each role has a display name used when presenting the role to users or
 * external consumers.
 * </p>
 */
@Getter
public enum RoleType {

    /**
     * Administrator role with full access to the application.
     */
    ADMIN("Admin"),

    /**
     * Collector role for standard application users.
     */
    COLLECTOR("Collector"),

    /**
     * Demo role intended for demonstration or limited-access users.
     */
    DEMO("Demo");

    private final String displayName;

    /**
     * Creates a role type with the specified display name.
     *
     * @param displayName
     *            the human-readable name of the role
     */
    RoleType(String displayName) {
        this.displayName = displayName;
    }
}
