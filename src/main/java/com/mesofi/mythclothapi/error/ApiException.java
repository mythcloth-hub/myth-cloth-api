package com.mesofi.mythclothapi.error;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {

  @Serial private static final long serialVersionUID = -7517595644718400266L;
  private final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
  private final String causeDetail;

  public ApiException(String message) {
    super(message);
    this.causeDetail = message;
  }

  public ApiException(String message, String causeDetail) {
    super(message);
    this.causeDetail = causeDetail;
  }
}
