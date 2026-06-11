package com.mesofi.mythclothapi.security.permissions;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.security.permissions.dto.PermissionReq;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PermissionController {

  private final PermissionService service;

  @PostMapping
  public ResponseEntity<PermissionResp> createPermission(
      @Valid @RequestBody PermissionReq permissionRequest) {
    PermissionResp response = service.createPermission(permissionRequest);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest() // e.g. /api/permissions
            .path("/{id}") // append /{id}
            .buildAndExpand(response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  public PermissionResp retrievePermission(@PathVariable Long id) {
    return service.retrievePermission(id);
  }

  @GetMapping
  public List<PermissionResp> retrievePermissions() {
    return service.retrievePermissions();
  }

  @PutMapping("/{id}")
  public ResponseEntity<PermissionResp> updatePermission(
      @PathVariable Long id, @Valid @RequestBody PermissionReq permissionRequest) {
    PermissionResp updated = service.updatePermission(id, permissionRequest);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> removePermission(@PathVariable Long id) {
    service.removePermission(id);
    return ResponseEntity.noContent().build();
  }
}
