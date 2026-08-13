package com.mesofi.mythclothapi.collectorscollections.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

class CollectorCollectionAlreadyExistsExceptionTest {

    @Test
    void constructor_shouldSetNameCorrectly_whenCreatedWithGivenName() {
        String name = "gold-saints";

        CollectorCollectionAlreadyExistsException exception = new CollectorCollectionAlreadyExistsException(name);

        assertThat(exception.getName()).isEqualTo(name);
    }

    @Test
    void constructor_shouldSetMessageCorrectly_whenCreated() {
        CollectorCollectionAlreadyExistsException exception = new CollectorCollectionAlreadyExistsException(
                "gold-saints");

        assertThat(exception.getMessage()).isEqualTo("Collector collection with name 'gold-saints' already exists");
    }

    @Test
    void getStatus_shouldReturnConflict_whenCalled() {
        CollectorCollectionAlreadyExistsException exception = new CollectorCollectionAlreadyExistsException(
                "gold-saints");

        HttpStatus status = exception.getStatus();

        assertThat(status).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getTitle_shouldReturnCollectorCollectionAlreadyExists_whenCalled() {
        CollectorCollectionAlreadyExistsException exception = new CollectorCollectionAlreadyExistsException(
                "gold-saints");

        assertThat(exception.getTitle()).isEqualTo("Collector collection already exists");
    }

    @Test
    void getErrorCode_shouldReturnCollectorCollectionAlreadyExists_whenCalled() {
        CollectorCollectionAlreadyExistsException exception = new CollectorCollectionAlreadyExistsException(
                "gold-saints");

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COLLECTOR_COLLECTION_ALREADY_EXISTS);
    }

    @Test
    void exception_shouldBeInstanceOfApiException_whenCreated() {
        CollectorCollectionAlreadyExistsException exception = new CollectorCollectionAlreadyExistsException(
                "gold-saints");

        assertThat(exception).isInstanceOf(ApiException.class);
    }

    @Test
    void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
        CollectorCollectionAlreadyExistsException exception = new CollectorCollectionAlreadyExistsException(
                "gold-saints");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructor_shouldHandleNullName_whenCreatedWithNullName() {
        CollectorCollectionAlreadyExistsException exception = new CollectorCollectionAlreadyExistsException(null);

        assertThat(exception.getName()).isNull();
        assertThat(exception.getMessage()).isEqualTo("Collector collection with name 'null' already exists");
    }
}
