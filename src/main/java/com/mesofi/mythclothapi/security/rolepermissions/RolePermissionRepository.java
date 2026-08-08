package com.mesofi.mythclothapi.security.rolepermissions;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.security.rolepermissions.model.RolePermission;

/** Repository for managing {@link RolePermission} persistence operations. */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    Optional<RolePermission> findByRoleAndPermission(String role, String permission);
}
