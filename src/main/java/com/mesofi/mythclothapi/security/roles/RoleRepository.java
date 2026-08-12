package com.mesofi.mythclothapi.security.roles;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.security.roles.model.Role;

/**
 * Repository for managing {@link Role} entities.
 *
 * <p>
 * Provides standard CRUD operations through {@link JpaRepository} and supports
 * retrieving roles by their unique name.
 * </p>
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Retrieves a role by its name.
     *
     * @param name
     *            the name of the role
     * @return an {@link Optional} containing the matching role, or an empty
     *         {@link Optional} if no role exists with the specified name
     */
    Optional<Role> findByName(String name);
}
