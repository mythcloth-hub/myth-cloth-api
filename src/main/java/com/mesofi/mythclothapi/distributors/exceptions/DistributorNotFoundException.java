package com.mesofi.mythclothapi.distributors.exceptions;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class DistributorNotFoundException extends ApiException {
  private static final long serialVersionUID = -4170723581171178442L;
  private final Long id;

  public DistributorNotFoundException(Long id) {
    super("Distributor not found");
    this.id = id;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
