package com.mesofi.mythclothapi.security.permissions.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class PermissionNotFoundException extends ApiException {
  @Serial private static final long serialVersionUID = -4170723581171178442L;
  private final Long id;

  public PermissionNotFoundException(Long id) {
    super("Permission not found");
    this.id = id;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
