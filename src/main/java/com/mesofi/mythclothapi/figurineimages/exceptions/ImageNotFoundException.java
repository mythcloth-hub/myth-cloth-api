package com.mesofi.mythclothapi.figurineimages.exceptions;

import java.io.Serial;
import java.net.URI;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

import lombok.Getter;

/**
 * Exception thrown when an image cannot be found.
 *
 * <p>
 * This exception is raised when attempting to retrieve or process an image
 * resource that does not exist or is not available at the specified location.
 *
 * <p>
 * This exception is translated into a {@link HttpStatus#NOT_FOUND} API response
 * and provides a specific {@link ErrorCode#FIGURINE_IMAGE_NOT_FOUND} error code
 * that clients can use to handle missing image resources programmatically.
 * </p>
 */
@Getter
public class ImageNotFoundException extends ApiException {

    @Serial
    private static final long serialVersionUID = -4170723581171178442L;

    /**
     * URI of the image that could not be found.
     */
    private final URI uri;

    /**
     * Creates a new exception for a missing image resource.
     *
     * @param uri
     *            URI of the image that was not found
     */
    public ImageNotFoundException(URI uri) {
        super("Image with URI '%s' was not found".formatted(uri));
        this.uri = uri;
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
     * @return {@code Image not found}
     */
    @Override
    public String getTitle() {
        return "Image not found";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#FIGURINE_IMAGE_NOT_FOUND}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.FIGURINE_IMAGE_NOT_FOUND;
    }
}
