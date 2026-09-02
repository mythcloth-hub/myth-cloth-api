package com.mesofi.mythclothapi.collectorscollections.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a collector account has reached the limit of collector
 * collections.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#BAD_REQUEST} API
 * response and provides a specific
 * {@link ErrorCode#COLLECTOR_COLLECTION_LIMIT_REACHED} error code that clients
 * can use to handle collector collection limit reached scenarios
 * programmatically.
 * </p>
 */
@Getter
public class CollectorCollectionLimitReachedException extends ApiException {

    @Serial
    private static final long serialVersionUID = 5915438291263890233L;

    /**
     * Identifier of the collector account that has reached the limit of collector
     * collections.
     */
    private final Long collectorId;

    /**
     * Creates a new exception for a collector account that has reached the limit of
     * collector collections.
     * 
     * @param collectorId
     *            identifier of the collector account that has reached the limit
     * @param maxCollections
     *            the maximum number of collector collections allowed for the
     *            collector account
     */
    public CollectorCollectionLimitReachedException(Long collectorId, int maxCollections) {
        super("Collector account with ID '%s' has reached the limit of collector collections: %d".formatted(collectorId,
                maxCollections));
        this.collectorId = collectorId;
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
     * @return {@code Collector collection limit reached}
     */
    @Override
    public String getTitle() {
        return "Collector collection limit reached";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#COLLECTOR_COLLECTION_LIMIT_REACHED}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.COLLECTOR_COLLECTION_LIMIT_REACHED;
    }
}
