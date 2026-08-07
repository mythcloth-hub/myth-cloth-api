package com.mesofi.mythclothapi.figurineimages.exceptions;

import java.io.Serial;
import java.net.URI;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when attempting to create a figurine image that already
 * exists.
 *
 * <p>
 * This exception is raised when an image with the same URI is already
 * registered for a figurine.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#CONFLICT} API response
 * and provides a specific {@link ErrorCode#FIGURINE_IMAGE_ALREADY_EXISTS} error
 * code that clients can use to handle duplicate image resources
 * programmatically.
 * </p>
 */
@Getter
public class ImageAlreadyExistsException extends ApiException {

    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * URI of the image that already exists.
     */
    private final URI uri;

    /**
     * Creates a new exception for an existing image resource.
     *
     * @param uri
     *            URI of the duplicated image
     */
    public ImageAlreadyExistsException(URI uri) {
        super("Image with URI '%s' already exists".formatted(uri));
        this.uri = uri;
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
     *
     * @return {@code Figurine image already exists}
     */
    @Override
    public String getTitle() {
        return "Figurine image already exists";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#FIGURINE_IMAGE_ALREADY_EXISTS}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.FIGURINE_IMAGE_ALREADY_EXISTS;
    }
}
