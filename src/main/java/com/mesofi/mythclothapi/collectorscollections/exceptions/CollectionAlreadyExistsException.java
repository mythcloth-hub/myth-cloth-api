package com.mesofi.mythclothapi.collectorscollections.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class CollectionAlreadyExistsException extends ApiException {
  @Serial private static final long serialVersionUID = -4170723581171178442L;
  private final String name;

  public CollectionAlreadyExistsException(String name) {
    super("Collection with name '%s' already exists".formatted(name));
    this.name = name;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }
}
