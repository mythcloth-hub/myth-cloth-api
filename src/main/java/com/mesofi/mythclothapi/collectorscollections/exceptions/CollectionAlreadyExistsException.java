package com.mesofi.mythclothapi.collectorscollections.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

public class CollectionAlreadyExistsException extends ApiException {
  @Serial private static final long serialVersionUID = -4170723581171178442L;

  public CollectionAlreadyExistsException(String name) {
    super("Collection with name '%s' already exists".formatted(name));
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }
}
