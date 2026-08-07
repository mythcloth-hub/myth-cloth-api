package com.mesofi.mythclothapi.distributors.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a distributor cannot be found.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#DISTRIBUTOR_NOT_FOUND} error code
 * that clients can use to handle missing distributor resources
 * programmatically.
 * </p>
 */
@Getter
public class DistributorNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * Identifier of the distributor that could not be found.
     */
    private final Long id;

    /**
     * Creates a new exception for a missing distributor.
     *
     * @param id
     *            identifier of the distributor that was not found
     */
    public DistributorNotFoundException(Long id) {
        super("Distributor with id %s was not found".formatted(id));
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
     * @return {@code Distributor not found}
     */
    @Override
    public String getTitle() {
        return "Distributor not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#DISTRIBUTOR_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.DISTRIBUTOR_NOT_FOUND;
    }
}
