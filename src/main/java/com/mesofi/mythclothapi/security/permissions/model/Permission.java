package com.mesofi.mythclothapi.security.permissions.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.mesofi.mythclothapi.common.Auditable;
import com.mesofi.mythclothapi.security.rolepermissions.RolePermission;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a permission that defines a specific operation that can be
 * performed within the application.
 *
 * <p>
 * Permissions are assigned to {@code Role} entities through
 * {@code RolePermission} associations. A role can therefore grant multiple
 * permissions to users assigned to that role.
 * </p>
 *
 * <p>
 * Permission names follow a resource-action convention, where the resource
 * identifies the protected functionality and the action identifies the
 * operation that is allowed.
 * </p>
 *
 * <p>
 * Examples include {@code permissions:read}, {@code permissions:write},
 * {@code permissions:update}, and {@code permissions:delete}.
 * </p>
 *
 * <p>
 * This entity extends {@link Auditable}, inheriting the common auditing fields
 * used to track the creation and modification of domain entities.
 * </p>
 */
@Getter
@Setter
@Entity
@Table(name = "permissions")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Permission extends Auditable {

    /**
     * The unique name that identifies the permission.
     *
     * <p>
     * The name follows a resource-action format, such as {@code figurines:read} or
     * {@code figurines:update}, and is used to identify the operation granted by
     * the permission.
     * </p>
     *
     * <p>
     * This field is mandatory and must be unique.
     * </p>
     */
    @EqualsAndHashCode.Include
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    // RolePermission.permission
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RolePermission> roles = new ArrayList<>();
}
