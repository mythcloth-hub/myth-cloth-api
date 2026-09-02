package com.mesofi.mythclothapi.collectors.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a collector email could not be found.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#COLLECTOR_EMAIL_NOT_FOUND} error
 * code that clients can use to handle the error programmatically.
 * </p>
 */
@Getter
public class CollectorEmailNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = 6649970713288460120L;

    /**
     * Creates a new exception for invalid collector credentials.
     */
    public CollectorEmailNotFoundException() {
        super("Collector email not found");
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
        return "Collector email not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#COLLECTOR_EMAIL_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.COLLECTOR_EMAIL_NOT_FOUND;
    }
}
