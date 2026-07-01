package com.mesofi.mythclothapi.collectorspurchases.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

/**
 * Exception thrown when a collector purchase cannot be found.
 *
 * <p>This exception is raised when attempting to retrieve, update, or delete a purchase that does
 * not exist or does not belong to the specified collector.
 *
 * <p>The exception is mapped to an HTTP {@link HttpStatus#NOT_FOUND} response.
 */
@Getter
public class CollectorPurchaseNotFoundException extends ApiException {
  @Serial private static final long serialVersionUID = 2115486705785649051L;

  /**
   * Creates a new exception for a missing collector purchase.
   *
   * @param id identifier of the purchase that could not be found
   */
  public CollectorPurchaseNotFoundException(Long id) {
    super("Collector purchase not found for this id: " + id);
  }

  /**
   * Returns the HTTP status associated with this exception.
   *
   * @return {@link HttpStatus#NOT_FOUND}
   */
  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
