package com.mesofi.mythclothapi.anniversaries;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ErrorCode;

class AnniversaryNotFoundExceptionTest {

    @Test
    void constructor_shouldPopulateState_andOverrideApiMetadata() {
        AnniversaryNotFoundException exception = new AnniversaryNotFoundException(42L);

        assertThat(exception.getId()).isEqualTo(42L);
        assertThat(exception.getMessage()).isEqualTo("Anniversary with id 42 was not found");
        assertThat(exception.getDetail()).isEqualTo("Anniversary with id 42 was not found");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getTitle()).isEqualTo("Anniversary not found");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FIGURINE_ANNIVERSARY_NOT_FOUND);
    }

    @Test
    void constructor_shouldAllowNullId() {
        AnniversaryNotFoundException exception = new AnniversaryNotFoundException(null);

        assertThat(exception.getId()).isNull();
        assertThat(exception.getMessage()).isEqualTo("Anniversary with id null was not found");
    }
}
