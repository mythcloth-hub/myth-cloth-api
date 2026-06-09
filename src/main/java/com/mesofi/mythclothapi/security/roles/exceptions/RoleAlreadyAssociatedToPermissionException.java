package com.mesofi.mythclothapi.security.roles.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class RoleAlreadyAssociatedToPermissionException extends ApiException {
  @Serial private static final long serialVersionUID = -4170723581171178442L;
  private final Long roleId;
  private final Long permissionId;

  public RoleAlreadyAssociatedToPermissionException(Long roleId, Long permissionId) {
    super("Role with Id: %s has a permission associated: %s".formatted(roleId, permissionId));
    this.roleId = roleId;
    this.permissionId = permissionId;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }
}
