package com.mesofi.mythclothapi.figurines.exceptions;

import java.io.Serial;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

/**
 * Indicates that an error occurred while importing figurines.
 *
 * <p>
 * This exception is raised when the figurine import process cannot be completed
 * successfully.
 * </p>
 */
public class FigurineImportException extends ApiException {

    @Serial
    private static final long serialVersionUID = -337615418722868201L;

    /**
     * Creates a new figurine import exception.
     */
    public FigurineImportException() {
        super("There was an error importing figurines.");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Figurine Import Error}
     */
    @Override
    public String getTitle() {
        return "Figurine Import Error";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link ErrorCode#FIGURINE_IMPORT_ERROR}
     */
    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.FIGURINE_IMPORT_ERROR;
    }
}