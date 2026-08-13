package com.mesofi.mythclothapi.figurines.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ErrorCode;
import com.mesofi.mythclothapi.figurines.FigurineNotFoundException;

class FigurineNotFoundExceptionTest {

    @Test
    void constructor_shouldPopulateState_andOverrideApiMetadata() {
        FigurineNotFoundException exception = new FigurineNotFoundException(42L);

        assertThat(exception.getId()).isEqualTo(42L);
        assertThat(exception.getMessage()).isEqualTo("Figurine with id 42 was not found");
        assertThat(exception.getDetail()).isEqualTo("Figurine with id 42 was not found");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getTitle()).isEqualTo("Figurine not found");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FIGURINE_NOT_FOUND);
    }

    @Test
    void constructor_shouldAllowNullId() {
        FigurineNotFoundException exception = new FigurineNotFoundException(null);

        assertThat(exception.getId()).isNull();
        assertThat(exception.getMessage()).isEqualTo("Figurine with id null was not found");
    }
}
