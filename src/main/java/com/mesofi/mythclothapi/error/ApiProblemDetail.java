package com.mesofi.mythclothapi.error;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Utility class for creating standardized API error responses using Spring's
 * {@link ProblemDetail}.
 *
 * <p>
 * This class centralizes the creation of {@link ProblemDetail} instances,
 * ensuring that all API errors share a consistent structure and include common
 * metadata such as the timestamp when the error occurred.
 * </p>
 *
 * <p>
 * When the error originates from an {@link ApiException}, the corresponding
 * application-specific error code is also included in the response, allowing
 * clients to handle errors programmatically without relying on error messages.
 * </p>
 */
public final class ApiProblemDetail {

    private ApiProblemDetail() {
    }

    /**
     * Creates a generic {@link ProblemDetail} response.
     *
     * <p>
     * This method is intended for errors that are not represented by a custom
     * {@link ApiException}. The returned problem detail contains the supplied HTTP
     * status, title, detail, and a timestamp.
     * </p>
     *
     * @param status
     *            HTTP status associated with the error
     * @param title
     *            short, human-readable summary of the problem
     * @param detail
     *            detailed explanation of the error
     *
     * @return a configured {@link ProblemDetail} instance
     */
    public static ProblemDetail of(HttpStatus status, String title, String detail) {
        return createProblemDetail(status, title, detail);
    }

    /**
     * Creates a {@link ProblemDetail} response from an {@link ApiException}.
     *
     * <p>
     * In addition to the standard problem detail fields, the returned response
     * includes the application-specific {@code errorCode} defined by the exception.
     * </p>
     *
     * @param apiException
     *            application exception containing the error metadata
     *
     * @return a configured {@link ProblemDetail} instance
     */
    public static ProblemDetail of(ApiException apiException) {
        ProblemDetail pd = createProblemDetail(apiException.getStatus(), apiException.getTitle(),
                apiException.getDetail());

        pd.setProperty("errorCode", apiException.getErrorCode().name());
        return pd;
    }

    /**
     * Creates the common {@link ProblemDetail} structure shared by all API errors.
     *
     * <p>
     * The returned instance contains the supplied HTTP status, title, detail, and
     * the timestamp at which the problem detail was created.
     * </p>
     *
     * @param status
     *            HTTP status associated with the error
     * @param title
     *            short, human-readable summary of the problem
     * @param detail
     *            detailed explanation of the error
     *
     * @return a configured {@link ProblemDetail} instance
     */
    private static ProblemDetail createProblemDetail(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        pd.setDetail(detail);
        pd.setProperty("timestamp", Instant.now());

        return pd;
    }
}
