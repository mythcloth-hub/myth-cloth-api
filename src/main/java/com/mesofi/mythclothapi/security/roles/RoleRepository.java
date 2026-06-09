package com.mesofi.mythclothapi.security.roles;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mesofi.mythclothapi.security.roles.model.Role;

/** Repository for managing {@link Role} persistence operations. */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByDescription(String description);
}
