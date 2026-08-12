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

/**
 * Service responsible for managing application permissions.
 *
 * <p>
 * Provides operations for creating, retrieving, updating, and removing
 * permissions used by the application's authorization system.
 * </p>
 *
 * <p>
 * Permission names are required to be unique when creating a new permission.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository repository;
    private final SecurityMapper mapper;

    /**
     * Creates a new application permission.
     *
     * <p>
     * The permission name must be unique. If a permission with the same name
     * already exists, a {@link PermissionAlreadyExistsException} is thrown.
     * </p>
     *
     * @param request
     *            the request containing the permission information
     * @return the newly created permission
     * @throws PermissionAlreadyExistsException
     *             if a permission with the requested name already exists
     */
    @Transactional
    public PermissionResp createPermission(PermissionReq request) {
        log.info("Creating permission: {}", request.description());

        Permission entity = mapper.toPermission(request);

        // Ensure that permission names are unique.
        repository.findByName(entity.getName()).ifPresent(existing -> {
            throw new PermissionAlreadyExistsException(existing.getName());
        });

        var saved = repository.save(entity);
        return mapper.toPermissionResp(saved);
    }

    /**
     * Retrieves a permission by its identifier.
     *
     * @param id
     *            the unique identifier of the permission
     * @return the requested permission
     * @throws PermissionNotFoundException
     *             if no permission exists with the specified identifier
     */
    @Transactional(readOnly = true)
    public PermissionResp retrievePermission(Long id) {
        return repository.findById(id).map(mapper::toPermissionResp)
                .orElseThrow(() -> new PermissionNotFoundException(id));
    }

    /**
     * Retrieves all application permissions ordered by their identifier.
     *
     * @return a list containing all application permissions
     */
    @Transactional(readOnly = true)
    public List<PermissionResp> retrievePermissions() {
        return repository.findAll(Sort.by("id")).stream().map(mapper::toPermissionResp).toList();
    }

    /**
     * Updates an existing application permission.
     *
     * <p>
     * The permission name is replaced with the description provided in the request.
     * </p>
     *
     * @param id
     *            the unique identifier of the permission to update
     * @param request
     *            the request containing the updated permission information
     * @return the updated permission
     * @throws PermissionNotFoundException
     *             if no permission exists with the specified identifier
     */
    @Transactional
    public PermissionResp updatePermission(Long id, PermissionReq request) {
        log.info("Updating permission {} to {}", id, request.description());

        var existing = repository.findById(id).orElseThrow(() -> new PermissionNotFoundException(id));

        existing.setName(request.description());

        var saved = repository.save(existing);
        return mapper.toPermissionResp(saved);
    }

    /**
     * Removes an existing permission.
     *
     * @param id
     *            the unique identifier of the permission to remove
     * @throws PermissionNotFoundException
     *             if no permission exists with the specified identifier
     */
    @Transactional
    public void removePermission(Long id) {
        log.warn("Removing permission {}", id);

        if (!repository.existsById(id)) {
            throw new PermissionNotFoundException(id);
        }

        repository.deleteById(id);
    }
}