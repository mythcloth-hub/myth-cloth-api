package com.mesofi.mythclothapi.error;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Base exception class for application-specific errors.
 *
 * <p>
 * Provides common information required to build API error responses, including
 * an HTTP status, error detail, and a machine-readable {@link ErrorCode}.
 * Subclasses may override the HTTP status when a different response code is
 * required.
 * </p>
 */
@Getter
public abstract class ApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7517595644718400266L;

    private final String detail;

    /**
     * Creates a new API exception using the provided message as the error detail.
     *
     * @param message
     *            error message
     */
    public ApiException(String message) {
        super(message);
        this.detail = message;
    }

    /**
     * Creates a new API exception with a separate message and detail.
     *
     * @param message
     *            exception message
     * @param detail
     *            error detail exposed to API consumers
     */
    public ApiException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    /**
     * Returns the HTTP status associated with this exception.
     *
     * <p>
     * Defaults to {@link HttpStatus#INTERNAL_SERVER_ERROR}. Subclasses may override
     * this method when they represent a specific HTTP error.
     * </p>
     *
     * @return HTTP status
     */
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Returns the title displayed in the API problem detail response.
     *
     * @return error title
     */
    public String getTitle() {
        return getMessage();
    }

    /**
     * Returns the application-specific error code.
     *
     * @return error code
     */
    public abstract ErrorCode getErrorCode();
}
