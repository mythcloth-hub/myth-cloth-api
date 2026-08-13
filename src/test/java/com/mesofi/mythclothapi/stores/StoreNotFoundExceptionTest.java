package com.mesofi.mythclothapi.stores;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ErrorCode;

class StoreNotFoundExceptionTest {

    @Test
    void constructor_shouldPopulateState_andOverrideApiMetadata() {
        StoreNotFoundException exception = new StoreNotFoundException(42L);

        assertThat(exception.getId()).isEqualTo(42L);
        assertThat(exception.getMessage()).isEqualTo("Store with id 42 was not found");
        assertThat(exception.getDetail()).isEqualTo("Store with id 42 was not found");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getTitle()).isEqualTo("Store not found");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STORE_NOT_FOUND);
    }

    @Test
    void constructor_shouldAllowNullId() {
        StoreNotFoundException exception = new StoreNotFoundException(null);

        assertThat(exception.getId()).isNull();
        assertThat(exception.getMessage()).isEqualTo("Store with id null was not found");
    }
}
