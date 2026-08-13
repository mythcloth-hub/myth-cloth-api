package com.mesofi.mythclothapi.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class ApiProblemDetailTest {

    @Test
    void constructor_shouldBeAccessibleViaReflectionAndCreateObject() throws Exception {
        Constructor<ApiProblemDetail> constructor = ApiProblemDetail.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        ApiProblemDetail instance = constructor.newInstance();

        assertThat(instance).isNotNull();
    }

    @Test
    void of_withHttpStatus_shouldCreateProblemDetailWithTimestamp() {
        ProblemDetail result = ApiProblemDetail.of(HttpStatus.BAD_REQUEST, "Bad request", "Missing required field");

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Bad request");
        assertThat(result.getDetail()).isEqualTo("Missing required field");
        assertThat(result.getProperties()).containsKey("timestamp");
        assertThat(result.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    }

    @Test
    void of_withApiException_shouldUseExceptionMetadataAndAddErrorCode() {
        TestApiException exception = new TestApiException("Invalid payload", "The request body is invalid.");

        ProblemDetail result = ApiProblemDetail.of(exception);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(result.getTitle()).isEqualTo("Invalid payload");
        assertThat(result.getDetail()).isEqualTo("The request body is invalid.");
        assertThat(result.getProperties()).containsEntry("errorCode", ErrorCode.INVALID_TOKEN.name());
    }

    private static final class TestApiException extends ApiException {

        private static final long serialVersionUID = 1L;

        private TestApiException(String message, String detail) {
            super(message, detail);
        }

        @Override
        public HttpStatus getStatus() {
            return HttpStatus.UNAUTHORIZED;
        }

        @Override
        public ErrorCode getErrorCode() {
            return ErrorCode.INVALID_TOKEN;
        }
    }
}
