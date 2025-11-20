package com.mesofi.mythclothapi.references;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class ReferencePairNotFoundException extends ApiException {

  private static final long serialVersionUID = -7007970083830745467L;
  private final String name;

  public ReferencePairNotFoundException(String name) {
    super("Distributor not found");
    this.name = name;
  }
}
