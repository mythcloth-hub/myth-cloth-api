package com.mesofi.mythclothapi.security.permissions;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.security.permissions.model.Permission;

/**
 * Repository for managing {@link Permission} entities.
 *
 * <p>
 * Provides standard CRUD operations through {@link JpaRepository} and supports
 * retrieving permissions by their name.
 * </p>
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Retrieves a permission by its name.
     *
     * @param name
     *            the name of the permission
     * @return an {@link Optional} containing the matching permission, or an empty
     *         {@link Optional} if no permission exists with the specified name
     */
    Optional<Permission> findByName(String name);
}