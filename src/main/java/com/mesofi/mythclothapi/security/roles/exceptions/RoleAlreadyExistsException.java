package com.mesofi.mythclothapi.security.roles.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class RoleAlreadyExistsException extends ApiException {
  @Serial private static final long serialVersionUID = -4170723581171178442L;
  private final String description;

  public RoleAlreadyExistsException(String description) {
    super("Duplicate Role with description: '%s'".formatted(description));
    this.description = description;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }
}
