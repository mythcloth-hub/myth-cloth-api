package com.mesofi.mythclothapi.collectorspurchases;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

import java.time.LocalDate;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import com.mesofi.mythclothapi.support.ControllerBaseIT;

@Sql(scripts = "/cleanup-purchases-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CollectorPurchaseControllerIT extends ControllerBaseIT {

    private static final Logger log = LoggerFactory.getLogger(CollectorPurchaseControllerIT.class);

    private static final String PURCHASES = "/purchases";

    @Test
    @DisplayName("Register a new purchase")
    void registerNewPurchase() {
        CollectorPurchaseResp purchaseResp = registerPurchase();
        log.info("New purchase registered: {}", purchaseResp);
    }

    private CollectorPurchaseResp registerPurchase() {
        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.now(), "Mandarake");

        ResponseEntity<CollectorPurchaseResp> response = rest.post().uri(PURCHASES).body(request).retrieve()
                .toEntity(CollectorPurchaseResp.class);

        assertThat(response.getStatusCode()).isEqualTo(CREATED);

        CollectorPurchaseResp body = Objects.requireNonNull(response.getBody(),
                "Purchase response body should not be null");

        assertThat(body.purchaseId()).isNotNull();
        assertThat(body.purchaseId()).isPositive();
        assertThat(body.seller()).isEqualTo("Mandarake");

        return response.getBody();
    }
}
