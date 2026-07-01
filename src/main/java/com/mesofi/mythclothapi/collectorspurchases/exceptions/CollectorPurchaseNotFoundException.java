package com.mesofi.mythclothapi.collectorspurchases.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

@Getter
public class CollectorPurchaseNotFoundException extends ApiException {
  @Serial private static final long serialVersionUID = 2115486705785649051L;

  public CollectorPurchaseNotFoundException(Long id) {
    super("Collector purchase not found for this id: " + id);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
