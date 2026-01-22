package com.mesofi.mythclothapi.figurineimages.exceptions;

import java.io.Serial;
import java.net.URI;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class ImageNotFoundException extends ApiException {
  @Serial private static final long serialVersionUID = -4170723581171178442L;
  private final URI uri;

  public ImageNotFoundException(URI uri) {
    super("Image not found");
    this.uri = uri;
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
