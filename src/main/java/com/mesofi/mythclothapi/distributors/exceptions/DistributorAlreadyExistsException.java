package com.mesofi.mythclothapi.distributors.exceptions;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCodes;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DistributorAlreadyExistsException extends ApiException {
  private final String name;
  private final String country;

  public DistributorAlreadyExistsException(String name, String country) {
    super(
        "Distributor already exists: " + name + " - " + country,
        ErrorCodes.DISTRIBUTOR_ALREADY_EXISTS.name());
    this.name = name;
    this.country = country;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }
}
