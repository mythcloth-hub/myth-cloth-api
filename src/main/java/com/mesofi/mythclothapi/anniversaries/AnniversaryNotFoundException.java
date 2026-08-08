package com.mesofi.mythclothapi.anniversaries;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when an anniversary cannot be found.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#FIGURINE_ANNIVERSARY_NOT_FOUND}
 * error code that clients can use to handle missing anniversary resources
 * programmatically.
 * </p>
 */
@Getter
public class AnniversaryNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * Identifier of the anniversary that could not be found.
     */
    private final Long id;

    /**
     * Creates a new exception for a missing anniversary.
     *
     * @param id
     *            identifier of the anniversary that was not found
     */
    public AnniversaryNotFoundException(Long id) {
        super("Anniversary with id %s was not found".formatted(id));
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
     * @return {@code Anniversary not found}
     */
    @Override
    public String getTitle() {
        return "Anniversary not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#FIGURINE_ANNIVERSARY_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.FIGURINE_ANNIVERSARY_NOT_FOUND;
    }
}
