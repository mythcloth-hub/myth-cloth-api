package com.mesofi.mythclothapi.figurines.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a figurine cannot be found.
 *
 * <p>
 * This exception is raised when attempting to retrieve, update, or delete a
 * figurine that does not exist.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#FIGURINE_NOT_FOUND} error code that
 * clients can use to handle missing figurine resources programmatically.
 * </p>
 */
@Getter
public class FigurineNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * Identifier of the figurine that could not be found.
     */
    private final Long id;

    /**
     * Creates a new exception for a missing figurine.
     *
     * @param id
     *            identifier of the figurine that was not found
     */
    public FigurineNotFoundException(Long id) {
        super("Figurine with id %s was not found".formatted(id));
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
     * @return {@code Figurine not found}
     */
    @Override
    public String getTitle() {
        return "Figurine not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#FIGURINE_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.FIGURINE_NOT_FOUND;
    }
}
