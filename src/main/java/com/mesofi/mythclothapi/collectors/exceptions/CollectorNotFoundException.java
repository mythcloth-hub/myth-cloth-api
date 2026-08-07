package com.mesofi.mythclothapi.collectors.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a collector cannot be found.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#COLLECTOR_NOT_FOUND} error code that
 * clients can use to handle the error programmatically.
 * </p>
 */
@Getter
public class CollectorNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = 8649970713288460120L;

    /**
     * Identifier of the collector that could not be found.
     */
    private final Long id;

    /**
     * Creates a new exception for a missing collector.
     *
     * @param id
     *            identifier of the collector that was not found
     */
    public CollectorNotFoundException(Long id) {
        super("Collector with id %s was not found".formatted(id));
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
     */
    @Override
    public String getTitle() {
        return "Collector not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#COLLECTOR_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.COLLECTOR_NOT_FOUND;
    }
}
