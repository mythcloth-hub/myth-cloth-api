package com.mesofi.mythclothapi.collectorspurchases;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ApiException;
import com.mesofi.mythclothapi.error.ErrorCode;

class CollectorPurchaseNotFoundExceptionTest {

    @Test
    void constructor_shouldSetIdCorrectly_whenCreatedWithGivenId() {
        Long id = 42L;

        CollectorPurchaseNotFoundException exception = new CollectorPurchaseNotFoundException(id);

        assertThat(exception.getId()).isEqualTo(id);
    }

    @Test
    void constructor_shouldSetMessageCorrectly_whenCreated() {
        CollectorPurchaseNotFoundException exception = new CollectorPurchaseNotFoundException(42L);

        assertThat(exception.getMessage()).isEqualTo("Collector purchase with id 42 was not found");
    }

    @Test
    void getStatus_shouldReturnNotFound_whenCalled() {
        CollectorPurchaseNotFoundException exception = new CollectorPurchaseNotFoundException(42L);

        HttpStatus status = exception.getStatus();

        assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getTitle_shouldReturnCollectorPurchaseNotFound_whenCalled() {
        CollectorPurchaseNotFoundException exception = new CollectorPurchaseNotFoundException(42L);

        assertThat(exception.getTitle()).isEqualTo("Collector purchase not found");
    }

    @Test
    void getErrorCode_shouldReturnCollectorPurchaseNotFound_whenCalled() {
        CollectorPurchaseNotFoundException exception = new CollectorPurchaseNotFoundException(42L);

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COLLECTOR_PURCHASE_NOT_FOUND);
    }

    @Test
    void exception_shouldBeInstanceOfApiException_whenCreated() {
        CollectorPurchaseNotFoundException exception = new CollectorPurchaseNotFoundException(42L);

        assertThat(exception).isInstanceOf(ApiException.class);
    }

    @Test
    void exception_shouldBeInstanceOfRuntimeException_whenCreated() {
        CollectorPurchaseNotFoundException exception = new CollectorPurchaseNotFoundException(42L);

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructor_shouldHandleNullId_whenCreatedWithNullId() {
        CollectorPurchaseNotFoundException exception = new CollectorPurchaseNotFoundException(null);

        assertThat(exception.getId()).isNull();
        assertThat(exception.getMessage()).isEqualTo("Collector purchase with id null was not found");
    }
}
