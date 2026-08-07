package com.mesofi.mythclothapi.stores;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a requested store cannot be found.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#STORE_NOT_FOUND} error code that
 * clients can use to handle missing store resources programmatically.
 * </p>
 */
@Getter
public class StoreNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * Identifier of the store that could not be found.
     */
    private final Long id;

    /**
     * Creates a new exception for a missing store.
     *
     * @param id
     *            identifier of the store that was not found
     */
    public StoreNotFoundException(Long id) {
        super("Store with id %s was not found".formatted(id));
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
     * @return {@code Store not found}
     */
    @Override
    public String getTitle() {
        return "Store not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#STORE_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.STORE_NOT_FOUND;
    }
}
