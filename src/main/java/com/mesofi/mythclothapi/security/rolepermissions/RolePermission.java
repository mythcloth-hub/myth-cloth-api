package com.mesofi.mythclothapi.security.rolepermissions;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.mesofi.mythclothapi.common.Auditable;
import com.mesofi.mythclothapi.security.permissions.model.Permission;
import com.mesofi.mythclothapi.security.roles.model.Role;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the association between a {@link Role} and a {@link Permission}.
 *
 * <p>
 * A role can have multiple permissions, and a permission can be assigned to
 * multiple roles. Each role-permission combination must be unique and is
 * enforced at the database level through a unique constraint.
 * </p>
 */
@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_role_permission", columnNames = {"role_id", "permission_id"}))
public class RolePermission extends Auditable {

    /**
     * The role associated with this permission assignment.
     */
    @ManyToOne(optional = false)
    private Role role;

    /**
     * The permission assigned to the role.
     */
    @ManyToOne(optional = false)
    private Permission permission;
}