package com.mesofi.mythclothapi.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException {
  private final String errorCode;
  private final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

  public ApiException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }
}
