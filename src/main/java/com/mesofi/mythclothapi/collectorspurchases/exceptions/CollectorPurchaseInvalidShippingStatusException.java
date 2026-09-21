package com.mesofi.mythclothapi.collectorspurchases.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a collector purchase has an invalid shipping status.
 *
 * <p>
 * This exception is raised when attempting to perform an operation on a
 * collector purchase that is not allowed due to its current shipping status.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#BAD_REQUEST} API
 * response and provides a specific
 * {@link ErrorCode#COLLECTOR_PURCHASE_INVALID_SHIPPING_STATUS} error code that
 * clients can use to handle invalid shipping status scenarios programmatically.
 * </p>
 */
@Getter
public class CollectorPurchaseInvalidShippingStatusException extends ApiException {

    @Serial
    private static final long serialVersionUID = 2115486705785649051L;

    /**
     * Creates a new exception for a collector purchase with an invalid shipping
     * status.
     */
    public CollectorPurchaseInvalidShippingStatusException() {
        super("Collector purchase has an invalid shipping status");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link HttpStatus#BAD_REQUEST}
     */
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Collector purchase has an invalid shipping status}
     */
    @Override
    public String getTitle() {
        return "Collector purchase has an invalid shipping status";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#COLLECTOR_PURCHASE_INVALID_SHIPPING_STATUS}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.COLLECTOR_PURCHASE_INVALID_SHIPPING_STATUS;
    }
}
