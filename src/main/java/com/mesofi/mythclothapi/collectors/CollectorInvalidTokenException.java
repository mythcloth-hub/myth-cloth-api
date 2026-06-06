package com.mesofi.mythclothapi.collectors;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

/** Exception raised when a social-provider token fails validation. */
@Getter
public class CollectorInvalidTokenException extends ApiException {

  @Serial private static final long serialVersionUID = -5327477189820058260L;

  /**
   * Creates an unauthorized-token exception with a provider-specific message.
   *
   * @param message validation failure detail
   */
  public CollectorInvalidTokenException(String message) {
    super(message);
  }

  /**
   * Returns the HTTP status mapped to invalid token errors.
   *
   * @return {@link HttpStatus#UNAUTHORIZED}
   */
  @Override
  public HttpStatus getStatus() {
    return HttpStatus.UNAUTHORIZED;
  }
}
