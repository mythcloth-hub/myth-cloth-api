package com.mesofi.mythclothapi.collectorspurchases;

import java.time.LocalDate;

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
    // @DisplayName("Login with Facebook provider")
    void loginWithUsingFacebookProvider() {
        CollectorPurchaseResp userLoginResp = loginWithProvider();
        log.info("User logged in with Facebook: {}", userLoginResp);
    }

    private CollectorPurchaseResp loginWithProvider() {
        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.now());

        ResponseEntity<CollectorPurchaseResp> response = rest.post().uri(PURCHASES).body(request).retrieve()
                .toEntity(CollectorPurchaseResp.class);

        // assertThat(response.getStatusCode()).isEqualTo(OK);
        // assertThat(response.getBody()).isNotNull();
        // assertThat(response.getBody().collectorId()).isPositive();
        // assertThat(response.getBody().displayName()).isEqualTo(expectedDisplayName);
        // assertThat(response.getBody().email()).isEqualTo(demoProperties.email());
        // assertThat(response.getBody().role()).isEqualTo("Collector");
        // assertThat(response.getBody().accessToken()).isNotBlank();
        // assertThat(response.getBody().tokenType()).isEqualTo("Bearer");
        // assertThat(response.getBody().expiresInSeconds()).isPositive();

        return response.getBody();
    }
}
