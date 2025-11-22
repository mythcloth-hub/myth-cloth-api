package com.mesofi.mythclothapi.references.exceptions;

import java.io.Serial;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class ReferencePairNotFoundException extends ApiException {

  @Serial private static final long serialVersionUID = -7007970083830745467L;
  private final String name;

  public ReferencePairNotFoundException(String name) {
    super("Distributor not found: " + name);
    this.name = name;
  }
}
