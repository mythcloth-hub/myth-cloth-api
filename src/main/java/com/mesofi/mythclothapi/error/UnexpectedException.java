package com.mesofi.mythclothapi.error;

import java.io.Serial;

import lombok.Getter;

/**
 * Exception thrown when an unexpected error occurs during application
 * processing.
 *
 * <p>
 * This exception is intended for errors that do not have a more specific
 * application-level exception or error code. It provides a generic
 * representation of unexpected failures that can be handled consistently by the
 * application's error-handling mechanism.
 * </p>
 *
 * <p>
 * The associated {@link ErrorCode#UNEXPECTED_ERROR} identifies the error as an
 * unexpected application failure.
 * </p>
 */
@Getter
public class UnexpectedException extends ApiException {

    @Serial
    private static final long serialVersionUID = -3021351651486829889L;

    /**
     * Creates an {@code UnexpectedException} with the specified error message.
     *
     * @param message
     *            a message describing the unexpected error
     */
    public UnexpectedException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Unexpected error occurred, try again later.";
    }

    /**
     * Returns the error code associated with this exception.
     *
     * @return {@link ErrorCode#UNEXPECTED_ERROR}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.UNEXPECTED_ERROR;
    }
}
