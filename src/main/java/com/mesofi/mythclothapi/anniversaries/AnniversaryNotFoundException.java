package com.mesofi.mythclothapi.anniversaries;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class AnniversaryNotFoundException extends ApiException {
  @Serial private static final long serialVersionUID = -4170723581171178442L;
  private final Long id;

  public AnniversaryNotFoundException(Long id) {
    super("Anniversary not found");
    this.id = id;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
