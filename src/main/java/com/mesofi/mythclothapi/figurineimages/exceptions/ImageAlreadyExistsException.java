package com.mesofi.mythclothapi.figurineimages.exceptions;

import java.io.Serial;
import java.net.URI;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class ImageAlreadyExistsException extends ApiException {
  @Serial private static final long serialVersionUID = -4170723581171178442L;
  private final URI uri;

  public ImageAlreadyExistsException(URI uri) {
    super("Image already exists");
    this.uri = uri;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}
