package com.mesofi.mythclothapi.collectorspurchases;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a collector purchase cannot be found.
 *
 * <p>
 * This exception is raised when attempting to retrieve, update, or delete a
 * purchase that does not exist or cannot be associated with the specified
 * collector.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#COLLECTOR_PURCHASE_NOT_FOUND} error
 * code that clients can use to handle missing collector purchase resources
 * programmatically.
 * </p>
 */
@Getter
public class CollectorPurchaseNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = 2115486705785649051L;

    /**
     * Identifier of the collector purchase that could not be found.
     */
    private final Long id;

    /**
     * Creates a new exception for a missing collector purchase.
     *
     * @param id
     *            identifier of the purchase that was not found
     */
    public CollectorPurchaseNotFoundException(Long id) {
        super("Collector purchase with id %s was not found".formatted(id));
        this.id = id;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link HttpStatus#NOT_FOUND}
     */
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Collector purchase not found}
     */
    @Override
    public String getTitle() {
        return "Collector purchase not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#COLLECTOR_PURCHASE_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.COLLECTOR_PURCHASE_NOT_FOUND;
    }
}
