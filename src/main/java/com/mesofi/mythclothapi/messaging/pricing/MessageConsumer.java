package com.mesofi.mythclothapi.messaging.pricing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.mesofi.mythclothapi.figurinestores.FigurineStoreService;
import com.mesofi.mythclothapi.messaging.RabbitMQConfig;
import com.mesofi.mythclothapi.messaging.pricing.model.LineUP;
import com.mesofi.mythclothapi.messaging.pricing.model.ListingStatus;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreListing;
import com.mesofi.mythclothapi.messaging.pricing.model.StoreName;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class MessageConsumer {

    private final FigurineStoreService figurineStoreService;

    @RabbitListener(queues = RabbitMQConfig.QueueNames.CRAWLER_QUEUE)
    public void handleEvent(Map<String, Object> payload) {
        log.info("=> Processing the following figurine '{}'", payload.get("productName"));

        StoreName store = StoreName.valueOf((String) payload.get("store"));
        LineUP lineUp = LineUP.valueOf((String) payload.get("lineUp"));
        String originalProductName = (String) payload.get("originalProductName");
        String productName = (String) payload.get("productName");
        String productImageUrl = (String) payload.get("productImageUrl");
        String productUrl = (String) payload.get("productUrl");
        BigDecimal price = getBigDecimal(payload.get("price"));
        BigDecimal discount = getBigDecimal(payload.get("discount"));
        BigDecimal discountedPrice = getBigDecimal(payload.get("discountedPrice"));
        Currency currency = Currency.getInstance((String) payload.get("currency"));
        ListingStatus status = ListingStatus.valueOf((String) payload.get("status"));
        Instant checkedAt = Instant.parse((String) payload.get("checkedAt"));

        StoreListing storeListing = new StoreListing(store, lineUp, originalProductName, productName, productImageUrl,
                productUrl, price, discount, discountedPrice, currency, status, checkedAt);

        figurineStoreService.processStorePricing(storeListing);

    }

    private BigDecimal getBigDecimal(Object number) {
        if (number == null) {
            return null;
        }
        if (number instanceof Double) {
            return BigDecimal.valueOf((Double) number);
        }
        if (number instanceof Integer) {
            return new BigDecimal((Integer) number);
        }
        if (number instanceof Long) {
            return new BigDecimal((Long) number);
        }
        throw new IllegalArgumentException("Unsupported price type: " + number.getClass());
    }
}
