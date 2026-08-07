package com.mesofi.mythclothapi.catalogs.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when a catalog repository cannot be found.
 *
 * <p>
 * This exception is raised when attempting to retrieve or operate on a catalog
 * repository that does not exist.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#CATALOG_REPOSITORY_NOT_FOUND} error
 * code that clients can use to handle missing catalog repository resources
 * programmatically.
 * </p>
 */
@Getter
public class CatalogRepositoryNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = -7007970083830745467L;

    /**
     * Name of the catalog repository that could not be found.
     */
    private final String name;

    /**
     * Creates a new exception for a missing catalog repository.
     *
     * @param name
     *            name of the repository that was not found
     */
    public CatalogRepositoryNotFoundException(String name) {
        super("Catalog repository '%s' was not found".formatted(name));
        this.name = name;
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
     * @return {@code Catalog repository not found}
     */
    @Override
    public String getTitle() {
        return "Catalog repository not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#CATALOG_REPOSITORY_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.CATALOG_REPOSITORY_NOT_FOUND;
    }
}
