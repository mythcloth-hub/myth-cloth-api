package com.mesofi.mythclothapi.security.permissions;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.security.permissions.model.Permission;

/** Repository for managing {@link Permission} persistence operations. */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
  Optional<Permission> findByDescription(String description);
}
