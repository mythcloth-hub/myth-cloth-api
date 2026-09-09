package com.mesofi.mythclothapi.collectorscollections.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class CollectionNotFoundException extends ApiException {
  @Serial private static final long serialVersionUID = -4170723581171178442L;
  private final Long id;

  public CollectionNotFoundException(Long id) {
    super("Collection with id %s was not found".formatted(id));
    this.id = id;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
