package com.mesofi.mythclothapi.collectorscollections.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when attempting to create a collector collection that
 * already exists.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#CONFLICT} API response
 * and provides a specific {@link ErrorCode#COLLECTOR_COLLECTION_ALREADY_EXISTS}
 * error code that clients can use to handle duplicate collector collection
 * creation attempts programmatically.
 * </p>
 */
@Getter
public class CollectorCollectionAlreadyExistsException extends ApiException {

    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * Name of the collector collection that already exists.
     */
    private final String name;

    /**
     * Creates a new exception for an existing collector collection.
     *
     * @param name
     *            name of the duplicated collector collection
     */
    public CollectorCollectionAlreadyExistsException(String name) {
        super("Collector collection with name '%s' already exists".formatted(name));
        this.name = name;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link HttpStatus#CONFLICT}
     */
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Collector collection already exists}
     */
    @Override
    public String getTitle() {
        return "Collector collection already exists";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#COLLECTOR_COLLECTION_ALREADY_EXISTS}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.COLLECTOR_COLLECTION_ALREADY_EXISTS;
    }
}
