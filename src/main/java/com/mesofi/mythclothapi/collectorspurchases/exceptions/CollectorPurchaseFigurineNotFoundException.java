package com.mesofi.mythclothapi.collectorspurchases.exceptions;

import java.io.Serial;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when one or more figurines associated with a collector
 * purchase cannot be found.
 *
 * <p>
 * This exception is raised when attempting to retrieve, update, or delete a
 * purchase that references figurines that do not exist or cannot be associated
 * with the specified collector.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific
 * {@link ErrorCode#COLLECTOR_PURCHASE_FIGURINE_NOT_FOUND} error code that
 * clients can use to handle missing collector purchase figurine resources
 * programmatically.
 * </p>
 */
@Getter
public class CollectorPurchaseFigurineNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = 3115486705785649051L;

    /**
     * Creates a new exception for a missing collector purchase.
     *
     * @param figurineIds
     *            identifiers of the figurines that were not found
     */
    public CollectorPurchaseFigurineNotFoundException(List<Long> figurineIds) {
        super("Collector purchase figurines with IDs %s were not found".formatted(figurineIds));
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
     * @return {@code Collector purchase figurines not found}
     */
    @Override
    public String getTitle() {
        return "Collector purchase figurines not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#COLLECTOR_PURCHASE_FIGURINE_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.COLLECTOR_PURCHASE_FIGURINE_NOT_FOUND;
    }
}
