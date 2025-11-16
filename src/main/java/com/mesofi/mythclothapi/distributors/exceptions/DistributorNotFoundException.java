package com.mesofi.mythclothapi.distributors.exceptions;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCodes;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DistributorNotFoundException extends ApiException {
  private final Long id;

  public DistributorNotFoundException(Long id) {
    super("Distributor not found given id: " + id, ErrorCodes.DISTRIBUTOR_NOT_FOUND.name());
    this.id = id;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
