package com.mesofi.mythclothapi.messaging.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mesofi.mythclothapi.catalogs.model.LineUpType;
import com.mesofi.mythclothapi.figurinestores.FigurineStoreService;
import com.mesofi.mythclothapi.messaging.pricing.model.ListingStatus;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreListing;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreName;

@ExtendWith(MockitoExtension.class)
class MessageConsumerTest {

    @InjectMocks
    private MessageConsumer consumer;

    @Mock
    private FigurineStoreService figurineStoreService;

    @Test
    void handleEvent_shouldConvertPayloadAndProcessStorePricing() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("store", "MYTH_SUPPLIES");
        payload.put("lineUp", "MYTH_CLOTH");
        payload.put("originalProductName", "Original Aries");
        payload.put("productName", "Aries");
        payload.put("productImageUrl", "https://example.com/aries.jpg");
        payload.put("productUrl", "https://example.com/aries");
        payload.put("price", "999.99");
        payload.put("discount", 10);
        payload.put("discountedPrice", "899.99");
        payload.put("currency", "USD");
        payload.put("status", "IN_STOCK");
        payload.put("preorder", true);
        payload.put("checkedAt", "2025-03-11T12:30:45Z");

        consumer.handleEvent(payload);

        ArgumentCaptor<StoreListing> captor = ArgumentCaptor.forClass(StoreListing.class);
        verify(figurineStoreService).processStorePricing(captor.capture());

        StoreListing listing = captor.getValue();
        assertThat(listing.store()).isEqualTo(StoreName.MYTH_SUPPLIES);
        assertThat(listing.lineUp()).isEqualTo(LineUpType.MYTH_CLOTH);
        assertThat(listing.originalProductName()).isEqualTo("Original Aries");
        assertThat(listing.productName()).isEqualTo("Aries");
        assertThat(listing.productImageUrl()).isEqualTo("https://example.com/aries.jpg");
        assertThat(listing.productUrl()).isEqualTo("https://example.com/aries");
        assertThat(listing.price()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(listing.discount()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(listing.discountedPrice()).isEqualByComparingTo(new BigDecimal("899.99"));
        assertThat(listing.currency()).isEqualTo(Currency.getInstance("USD"));
        assertThat(listing.status()).isEqualTo(ListingStatus.IN_STOCK);
        assertThat(listing.preorder()).isTrue();
        assertThat(listing.checkedAt()).isEqualTo(Instant.parse("2025-03-11T12:30:45Z"));
    }
}
