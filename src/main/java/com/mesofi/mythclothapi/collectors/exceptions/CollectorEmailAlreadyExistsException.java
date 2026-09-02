package com.mesofi.mythclothapi.collectors.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a collector email already exists.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#CONFLICT} API response
 * and provides a specific {@link ErrorCode#COLLECTOR_EMAIL_ALREADY_EXISTS}
 * error code that clients can use to handle the error programmatically.
 * </p>
 */
@Getter
public class CollectorEmailAlreadyExistsException extends ApiException {

    @Serial
    private static final long serialVersionUID = 7649970713288460120L;

    /**
     * Email of the collector that already exists.
     */
    private final String email;

    /**
     * Creates a new exception for an existing collector email.
     *
     * @param email
     *            email of the collector that already exists
     */
    public CollectorEmailAlreadyExistsException(String email) {
        super("Collector with email %s already exists".formatted(email));
        this.email = email;
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
     */
    @Override
    public String getTitle() {
        return "Collector email already exists";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#COLLECTOR_EMAIL_ALREADY_EXISTS}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.COLLECTOR_EMAIL_ALREADY_EXISTS;
    }
}
