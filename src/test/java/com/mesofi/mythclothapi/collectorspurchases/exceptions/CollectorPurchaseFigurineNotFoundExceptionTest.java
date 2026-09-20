package com.mesofi.mythclothapi.collectorspurchases.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.error.ErrorCode;

class CollectorPurchaseFigurineNotFoundExceptionTest {

    @Test
    void shouldSetStatusTitleAndErrorCodeCorrectly_whenCreatedWithGivenIds() {

        CollectorPurchaseFigurineNotFoundException exception = new CollectorPurchaseFigurineNotFoundException(
                List.of(1L, 2L));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getTitle()).isEqualTo("Collector purchase figurines not found");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COLLECTOR_PURCHASE_FIGURINE_NOT_FOUND);
    }

}
