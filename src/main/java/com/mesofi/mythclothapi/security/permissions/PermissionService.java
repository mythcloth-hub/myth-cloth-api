package com.mesofi.mythclothapi.security.permissions;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.security.SecurityMapper;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionReq;
import com.mesofi.mythclothapi.security.permissions.dto.PermissionResp;
import com.mesofi.mythclothapi.security.permissions.exceptions.PermissionAlreadyExistsException;
import com.mesofi.mythclothapi.security.permissions.exceptions.PermissionNotFoundException;
import com.mesofi.mythclothapi.security.permissions.model.Permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

  private final PermissionRepository repository;
  private final SecurityMapper mapper;

  @Transactional
  public PermissionResp createPermission(PermissionReq request) {
    log.info("Creating permission: {}", request.description());

    Permission entity = mapper.toPermission(request);
    // make sure the permissions contain different descriptions.
    repository
        .findByDescription(entity.getDescription())
        .ifPresent(
            existing -> {
              throw new PermissionAlreadyExistsException(entity.getDescription());
            });

    var saved = repository.save(entity);
    return mapper.toPermissionResp(saved);
  }

  @Transactional(readOnly = true)
  public PermissionResp retrievePermission(Long id) {
    return repository
        .findById(id)
        .map(mapper::toPermissionResp)
        .orElseThrow(() -> new PermissionNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public List<PermissionResp> retrievePermissions() {
    return repository.findAll(Sort.by("id")).stream().map(mapper::toPermissionResp).toList();
  }

  @Transactional
  public PermissionResp updatePermission(Long id, PermissionReq request) {
    log.info("Updating permission {} to {}", id, request.description());
    var existing = repository.findById(id).orElseThrow(() -> new PermissionNotFoundException(id));

    existing.setDescription(request.description());

    var saved = repository.save(existing);
    return mapper.toPermissionResp(saved);
  }

  @Transactional
  public void removePermission(Long id) {
    log.warn("Removing permission {}", id);

    if (!repository.existsById(id)) {
      throw new PermissionNotFoundException(id);
    }
    repository.deleteById(id);
  }
}
