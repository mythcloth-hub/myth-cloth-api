package com.mesofi.mythclothapi.security.roles;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.security.roles.dto.RoleReq;
import com.mesofi.mythclothapi.security.roles.dto.RoleResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

  private final RoleService service;

  @PostMapping
  @PreAuthorize("hasAuthority('roles:write')")
  public ResponseEntity<RoleResp> createRole(@Valid @RequestBody RoleReq roleRequest) {
    RoleResp response = service.createRole(roleRequest);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest() // e.g. /api/roles
            .path("/{id}") // append /{id}
            .buildAndExpand(response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('roles:read')")
  public RoleResp retrieveRole(@PathVariable Long id) {
    return service.retrieveRole(id);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('roles:read')")
  public List<RoleResp> retrieveRoles() {
    return service.retrieveRoles();
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('roles:update')")
  public ResponseEntity<RoleResp> updateRole(
      @PathVariable Long id, @Valid @RequestBody RoleReq roleRequest) {
    RoleResp updated = service.updateRole(id, roleRequest);
    return ResponseEntity.ok(updated);
  }
}
