package com.mesofi.mythclothapi.collectors;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class CollectorInvalidTokenException extends ApiException {

  @Serial private static final long serialVersionUID = -5327477189820058260L;

  public CollectorInvalidTokenException(String message) {
    super(message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.UNAUTHORIZED;
  }
}
