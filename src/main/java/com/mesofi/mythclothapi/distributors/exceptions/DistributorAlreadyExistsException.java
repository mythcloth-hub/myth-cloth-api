package com.mesofi.mythclothapi.distributors.exceptions;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class DistributorAlreadyExistsException extends ApiException {
  private static final long serialVersionUID = 2305428537502137069L;
  private final String name;
  private final String country;

  public DistributorAlreadyExistsException(String name, String country) {
    super("Distributor already exists", "Distributor already exists: " + name + " - " + country);
    this.name = name;
    this.country = country;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }
}
