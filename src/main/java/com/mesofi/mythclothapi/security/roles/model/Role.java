
package com.mesofi.mythclothapi.security.roles.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.common.Auditable;
import com.mesofi.mythclothapi.security.rolepermissions.model.RolePermission;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a security role within the application.
 *
 * <p>
 * A {@code Role} groups a set of permissions that define the operations a user
 * assigned to the role is authorized to perform.
 * </p>
 *
 * <p>
 * Each role is identified by a unique name and can be associated with multiple
 * {@link RolePermission} entries.
 * </p>
 *
 * <p>
 * This entity extends {@link Auditable}, inheriting the common auditing fields
 * used to track the creation and modification of domain entities.
 * </p>
 */
@Entity
@Getter
@Setter
@Table(name = "roles")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Role extends Auditable {

    /**
     * The unique name of the role.
     *
     * <p>
     * The role name identifies the security role and is used to associate it with
     * the permissions that determine the user's access privileges.
     * </p>
     *
     * <p>
     * Examples include {@code ADMIN}, {@code COLLECTOR}, and {@code DEMO_USER}.
     * </p>
     *
     * <p>
     * This field is mandatory, must be unique, and is limited to 200 characters.
     * </p>
     */
    @EqualsAndHashCode.Include
    @Column(nullable = false, length = 200, unique = true)
    private String name;

    /**
     * The permissions assigned to this role.
     *
     * <p>
     * Each {@link RolePermission} associates this role with a specific permission.
     * A role can have multiple permissions, which collectively define the
     * operations available to users assigned to the role.
     * </p>
     *
     * <p>
     * Permissions are managed through this relationship, with associated
     * {@link RolePermission} entities being removed when they are no longer
     * associated with the role.
     * </p>
     */
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RolePermission> permissions = new ArrayList<>();
}
