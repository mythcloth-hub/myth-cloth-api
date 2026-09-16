package com.mesofi.mythclothapi.collectorspurchases;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseReq;
import com.mesofi.mythclothapi.collectorspurchases.dto.CollectorPurchaseResp;
import com.mesofi.mythclothapi.collectorspurchases.model.PurchaseChannel;
import com.mesofi.mythclothapi.collectorspurchases.model.ShippingStatus;
import com.mesofi.mythclothapi.support.ControllerBaseIT;

@Sql(scripts = "/cleanup-purchases-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CollectorPurchaseControllerIT extends ControllerBaseIT {

    private static final Logger log = LoggerFactory.getLogger(CollectorPurchaseControllerIT.class);

    private static final String PURCHASES = "/purchases";

    @Test
    @DisplayName("Register new purchase for both channels")
    void registerNewPurchaseForBothChannels() {
        CollectorPurchaseResp onlinePurchaseResp = registerOnlinePurchase();
        log.info("New online purchase registered: {}", onlinePurchaseResp);

        CollectorPurchaseResp inStorePurchaseResp = registerInStorePurchase();
        log.info("New in-store purchase registered: {}", inStorePurchaseResp);
    }

    private CollectorPurchaseResp registerInStorePurchase() {
        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.now(), "RockShow", null,
                Currency.getInstance("MXN"), PurchaseChannel.PHYSICAL_STORE, null, null, null);

        CollectorPurchaseResp body = sendRequestAndGetResponse(request);
        assertThat(body.purchaseId()).isNotNull();
        assertThat(body.purchaseId()).isPositive();
        assertThat(body.seller()).isEqualTo("RockShow");
        assertThat(body.orderNumber()).isNull();
        assertThat(body.currency()).isEqualTo("MXN");
        assertThat(body.totalAmount()).isEqualTo(new BigDecimal("1"));
        assertThat(body.purchaseChannel()).isEqualTo(PurchaseChannel.PHYSICAL_STORE);
        assertThat(body.shippingStatus()).isNull();
        assertThat(body.trackingNumber()).isNull();
        assertThat(body.carrier()).isNull();
        assertThat(body.deliveredDate()).isNull();
        assertThat(body.shippedDate()).isNull();

        return body;
    }

    private CollectorPurchaseResp registerOnlinePurchase() {
        CollectorPurchaseReq request = new CollectorPurchaseReq(LocalDate.now(), "Mandarake", "XQKUHSCWV",
                Currency.getInstance("JPY"), PurchaseChannel.ONLINE, ShippingStatus.DELIVERED, "1ZV912320456954189",
                "FEDEX");

        CollectorPurchaseResp body = sendRequestAndGetResponse(request);
        assertThat(body.purchaseId()).isNotNull();
        assertThat(body.purchaseId()).isPositive();
        assertThat(body.seller()).isEqualTo("Mandarake");
        assertThat(body.orderNumber()).isEqualTo("XQKUHSCWV");
        assertThat(body.currency()).isEqualTo("JPY");
        assertThat(body.totalAmount()).isEqualTo(new BigDecimal("1"));
        assertThat(body.purchaseChannel()).isEqualTo(PurchaseChannel.ONLINE);
        assertThat(body.shippingStatus()).isEqualTo(ShippingStatus.DELIVERED);
        assertThat(body.trackingNumber()).isEqualTo("1ZV912320456954189");
        assertThat(body.carrier()).isEqualTo("FEDEX");
        assertThat(body.deliveredDate()).isNotNull();
        assertThat(body.shippedDate()).isNull();

        return body;
    }

    private CollectorPurchaseResp sendRequestAndGetResponse(CollectorPurchaseReq request) {
        ResponseEntity<CollectorPurchaseResp> response = rest.post().uri(PURCHASES).body(request).retrieve()
                .toEntity(CollectorPurchaseResp.class);

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        CollectorPurchaseResp body = Objects.requireNonNull(response.getBody(),
                "Purchase response body should not be null");

        assertThat(body).isNotNull();

        return body;
    }
}
