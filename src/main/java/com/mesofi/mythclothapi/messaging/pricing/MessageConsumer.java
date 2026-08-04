package com.mesofi.mythclothapi.messaging.pricing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mesofi.mythclothapi.catalogs.model.LineUpType;
import com.mesofi.mythclothapi.figurinestores.FigurineStoreService;
import com.mesofi.mythclothapi.messaging.RabbitMQConfig;
import com.mesofi.mythclothapi.messaging.pricing.model.ListingStatus;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreListing;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreName;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnProperty(name = "myth-cloth.rabbit.enabled", havingValue = "true", matchIfMissing = true)
public class MessageConsumer {

    private final FigurineStoreService figurineStoreService;

    @RabbitListener(queues = RabbitMQConfig.QueueNames.CRAWLER_QUEUE)
    public void handleEvent(Map<String, Object> payload) {
        log.info("=> Processing the following figurine '{}'", payload.get("productName"));

        StoreName store = StoreName.valueOf((String) payload.get("store"));
        LineUpType lineUp = LineUpType.valueOf((String) payload.get("lineUp"));
        String originalProductName = (String) payload.get("originalProductName");
        String productName = (String) payload.get("productName");
        String productImageUrl = (String) payload.get("productImageUrl");
        String productUrl = (String) payload.get("productUrl");
        BigDecimal price = getBigDecimal(payload.get("price"));
        BigDecimal discount = getBigDecimal(payload.get("discount"));
        BigDecimal discountedPrice = getBigDecimal(payload.get("discountedPrice"));
        Currency currency = Currency.getInstance((String) payload.get("currency"));
        ListingStatus status = ListingStatus.valueOf((String) payload.get("status"));
        boolean preorder = (Boolean) payload.get("preorder");
        Instant checkedAt = Instant.parse((String) payload.get("checkedAt"));

        StoreListing storeListing = new StoreListing(store, lineUp, originalProductName, productName, productImageUrl,
                productUrl, price, discount, discountedPrice, currency, status, preorder, checkedAt);

        figurineStoreService.processStorePricing(storeListing);
    }

    private BigDecimal getBigDecimal(Object number) {
        return switch (number) {
            case null -> null;
            case Double v -> BigDecimal.valueOf(v);
            case Integer i -> new BigDecimal(i);
            case Long l -> new BigDecimal(l);
            case String s -> new BigDecimal(s);
            default -> throw new IllegalArgumentException("Unsupported price type: " + number.getClass());
        };
    }
}
