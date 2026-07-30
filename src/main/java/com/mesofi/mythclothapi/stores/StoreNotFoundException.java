package com.mesofi.mythclothapi.stores;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;

import lombok.Getter;

/**
 * Exception thrown when a requested store cannot be found.
 * <p>
 * This exception is translated into an HTTP {@code 404 Not Found} response.
 */
@Getter
public class StoreNotFoundException extends ApiException {
    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * The identifier of the store that could not be found.
     */
    private final Long id;

    /**
     * Creates a new exception for a store that could not be found.
     *
     * @param id
     *            the identifier of the requested store
     */
    public StoreNotFoundException(Long id) {
        super("Store not found with id: " + id);
        this.id = id;
    }

    /**
     * Returns the HTTP status associated with this exception.
     *
     * @return {@link HttpStatus#NOT_FOUND}
     */
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
