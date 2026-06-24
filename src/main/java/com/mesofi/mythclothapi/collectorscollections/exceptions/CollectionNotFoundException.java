package com.mesofi.mythclothapi.collectorscollections.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

public class CollectionNotFoundException extends ApiException {
  @Serial private static final long serialVersionUID = -4170723581171178442L;

  public CollectionNotFoundException(Long id) {
    super("Collection with id %s was not found".formatted(id));
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
