package com.mesofi.mythclothapi.security.rolepermissions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link RolePermission} entities.
 *
 * <p>
 * Provides standard CRUD operations for role-permission associations through
 * {@link JpaRepository}.
 * </p>
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

}