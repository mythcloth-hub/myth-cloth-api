package com.mesofi.mythclothapi.collectorscollections.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

class CollectorCollectionNotFoundExceptionTest {

    @Test
    void constructor_shouldSetIdCorrectly_whenCreatedWithGivenId() {
        Long id = 42L;

        CollectorCollectionNotFoundException exception = new CollectorCollectionNotFoundException(id);

        assertThat(exception.getId()).isEqualTo(id);
    }

    @Test
    void constructor_shouldSetMessageCorrectly_whenCreated() {
        CollectorCollectionNotFoundException exception = new CollectorCollectionNotFoundException(42L);

        assertThat(exception.getMessage()).isEqualTo("Collector collection with id 42 was not found");
    }

    @Test
    void getStatus_shouldReturnNotFound_whenCalled() {
        CollectorCollectionNotFoundException exception = new CollectorCollectionNotFoundException(42L);

        HttpStatus status = exception.getStatus();

        assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getTitle_shouldReturnCollectorCollectionNotFound_whenCalled() {
        CollectorCollectionNotFoundException exception = new CollectorCollectionNotFoundException(42L);

        assertThat(exception.getTitle()).isEqualTo("Collector collection not found");
    }

    @Test
    void getErrorCode_shouldReturnCollectorCollectionNotFound_whenCalled() {
        CollectorCollectionNotFoundException exception = new CollectorCollectionNotFoundException(42L);

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COLLECTOR_COLLECTION_NOT_FOUND);
    }

    @Test
    void exception_shouldBeInstanceOfApiException_whenCreated() {
        CollectorCollectionNotFoundException exception = new CollectorCollectionNotFoundException(42L);

        assertThat(exception).isInstanceOf(ApiException.class);
    }

    @Test
    void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
        CollectorCollectionNotFoundException exception = new CollectorCollectionNotFoundException(42L);

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructor_shouldHandleNullId_whenCreatedWithNullId() {
        CollectorCollectionNotFoundException exception = new CollectorCollectionNotFoundException(null);

        assertThat(exception.getId()).isNull();
        assertThat(exception.getMessage()).isEqualTo("Collector collection with id null was not found");
    }
}
