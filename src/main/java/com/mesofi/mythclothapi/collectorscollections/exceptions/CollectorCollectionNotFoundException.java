package com.mesofi.mythclothapi.collectorscollections.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a collector collection cannot be found.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#COLLECTOR_COLLECTION_NOT_FOUND}
 * error code that clients can use to handle missing collector collection
 * resources programmatically.
 * </p>
 */
@Getter
public class CollectorCollectionNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = 1915438291263890233L;

    /**
     * Identifier of the collector collection that could not be found.
     */
    private final Long id;

    /**
     * Creates a new exception for a missing collector collection.
     *
     * @param id
     *            identifier of the collector collection that was not found
     */
    public CollectorCollectionNotFoundException(Long id) {
        super("Collector collection with id %s was not found".formatted(id));
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
     * @return {@code Collector collection not found}
     */
    @Override
    public String getTitle() {
        return "Collector collection not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#COLLECTOR_COLLECTION_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.COLLECTOR_COLLECTION_NOT_FOUND;
    }
}
