package com.mesofi.mythclothapi.collectorscollections.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

class CollectorCollectionLimitReachedExceptionTest {

    @Test
    void constructor_shouldSetCollectorIdCorrectly_whenCreatedWithGivenId() {
        Long collectorId = 42L;

        CollectorCollectionLimitReachedException exception = new CollectorCollectionLimitReachedException(collectorId,
                3);

        assertThat(exception.getCollectorId()).isEqualTo(collectorId);
    }

    @Test
    void constructor_shouldSetMessageCorrectly_whenCreated() {
        CollectorCollectionLimitReachedException exception = new CollectorCollectionLimitReachedException(42L, 3);

        assertThat(exception.getMessage())
                .isEqualTo("Collector account with ID '42' has reached the limit of collector collections: 3");
    }

    @Test
    void getStatus_shouldReturnBadRequest_whenCalled() {
        CollectorCollectionLimitReachedException exception = new CollectorCollectionLimitReachedException(42L, 3);

        HttpStatus status = exception.getStatus();

        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getTitle_shouldReturnCollectorCollectionLimitReached_whenCalled() {
        CollectorCollectionLimitReachedException exception = new CollectorCollectionLimitReachedException(42L, 3);

        assertThat(exception.getTitle()).isEqualTo("Collector collection limit reached");
    }

    @Test
    void getErrorCode_shouldReturnCollectorCollectionLimitReached_whenCalled() {
        CollectorCollectionLimitReachedException exception = new CollectorCollectionLimitReachedException(42L, 3);

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COLLECTOR_COLLECTION_LIMIT_REACHED);
    }

    @Test
    void exception_shouldBeInstanceOfApiException_whenCreated() {
        CollectorCollectionLimitReachedException exception = new CollectorCollectionLimitReachedException(42L, 3);

        assertThat(exception).isInstanceOf(ApiException.class);
    }

    @Test
    void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
        CollectorCollectionLimitReachedException exception = new CollectorCollectionLimitReachedException(42L, 3);

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructor_shouldHandleNullCollectorId_whenCreatedWithNullId() {
        CollectorCollectionLimitReachedException exception = new CollectorCollectionLimitReachedException(null, 3);

        assertThat(exception.getCollectorId()).isNull();
        assertThat(exception.getMessage())
                .isEqualTo("Collector account with ID 'null' has reached the limit of collector collections: 3");
    }
}
