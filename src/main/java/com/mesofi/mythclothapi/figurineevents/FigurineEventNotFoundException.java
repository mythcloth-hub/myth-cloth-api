package com.mesofi.mythclothapi.figurineevents;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a figurine event cannot be found.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#EVENT_NOT_FOUND} error code that
 * clients can use to handle the error programmatically.
 * </p>
 */
@Getter
public class FigurineEventNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * Identifier of the figurine event that could not be found.
     */
    private final Long id;

    /**
     * Creates a new exception for a missing figurine event.
     *
     * @param id
     *            identifier of the figurine event that was not found
     */
    public FigurineEventNotFoundException(Long id) {
        super("Figurine event with id %s was not found".formatted(id));
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
        return "Figurine event not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#EVENT_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.EVENT_NOT_FOUND;
    }
}
