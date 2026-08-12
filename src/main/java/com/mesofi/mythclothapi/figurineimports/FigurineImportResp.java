package com.mesofi.mythclothapi.figurineimports;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents the API response for a figurine import operation.
 *
 * <p>
 * Contains summary information about the import, including the number of
 * figurines imported, any error message produced during the operation, and the
 * time at which the import completed.
 * </p>
 *
 * <p>
 * The {@code errorMessage} field is omitted from the JSON response when it is
 * empty.
 * </p>
 *
 * @param id
 *            the unique identifier of the import record
 * @param imported
 *            the total number of figurines successfully imported
 * @param errorMessage
 *            the error message produced during the import, if any
 * @param completedAt
 *            the date and time at which the import completed
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineImportResp(long id, int imported, String errorMessage, Instant completedAt) {
}
