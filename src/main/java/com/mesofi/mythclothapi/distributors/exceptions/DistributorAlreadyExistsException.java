package com.mesofi.mythclothapi.distributors.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when attempting to create a distributor that already exists.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#CONFLICT} API response
 * and provides a specific {@link ErrorCode#DISTRIBUTOR_ALREADY_EXISTS} error
 * code that clients can use to handle duplicate distributor creation attempts
 * programmatically.
 * </p>
 */
@Getter
public class DistributorAlreadyExistsException extends ApiException {

    @Serial
    private static final long serialVersionUID = 2305428537502137069L;

    /**
     * Name of the distributor that already exists.
     */
    private final String name;

    /**
     * Country associated with the distributor that already exists.
     */
    private final String country;

    /**
     * Creates a new exception for an existing distributor.
     *
     * @param name
     *            name of the duplicated distributor
     * @param country
     *            country associated with the duplicated distributor
     */
    public DistributorAlreadyExistsException(String name, String country) {
        super("Distributor already exists", "Distributor '%s' already exists in country '%s'".formatted(name, country));
        this.name = name;
        this.country = country;
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
     * @return {@code Distributor already exists}
     */
    @Override
    public String getTitle() {
        return "Distributor already exists";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#DISTRIBUTOR_ALREADY_EXISTS}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.DISTRIBUTOR_ALREADY_EXISTS;
    }
}
