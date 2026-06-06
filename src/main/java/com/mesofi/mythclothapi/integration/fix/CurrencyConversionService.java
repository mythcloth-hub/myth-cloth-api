package com.mesofi.mythclothapi.integration.fix;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

/**
 * Converts monetary amounts between currencies using rates provided by {@link FxApiClient}.
 *
 * <p>Rates are cached in memory for one hour per currency pair to reduce outbound API calls and
 * keep conversion latency low.
 */
@Service
public class CurrencyConversionService {
  private static final Duration TTL = Duration.ofHours(1);

  private final FxApiClient fxApiClient;
  private final ConcurrentMap<String, CachedRate> cache = new ConcurrentHashMap<>();

  /**
   * Creates the service with the FX client dependency.
   *
   * @param fxApiClient client used to fetch fresh exchange rates
   */
  public CurrencyConversionService(FxApiClient fxApiClient) {
    this.fxApiClient = fxApiClient;
  }

  /**
   * Converts an amount from one currency into another.
   *
   * <p>If source and target currencies are the same, the original amount is returned unchanged.
   * Otherwise, the result is rounded to two decimal places using {@link RoundingMode#HALF_UP}.
   *
   * @param amount amount to convert
   * @param from source currency code (for example, {@code USD})
   * @param to target currency code (for example, {@code JPY})
   * @return converted amount
   */
  public BigDecimal convert(BigDecimal amount, String from, String to) {
    if (from.equalsIgnoreCase(to)) return amount;
    BigDecimal rate = getRate(from, to);
    return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
  }

  /** Retrieves a rate from cache when available and valid, or fetches and caches a fresh rate. */
  private BigDecimal getRate(String from, String to) {
    String key = from.toUpperCase() + "_" + to.toUpperCase();
    CachedRate current = cache.get(key);

    if (current != null && !current.isExpired()) {
      return current.rate();
    }

    BigDecimal fresh = fxApiClient.fetchRate(from, to);
    cache.put(key, new CachedRate(fresh, Instant.now().plus(TTL)));
    return fresh;
  }

  /** Immutable cached FX rate entry with expiration metadata. */
  private record CachedRate(BigDecimal rate, Instant expiresAt) {
    /**
     * @return {@code true} when this cached rate is past its expiration time
     */
    boolean isExpired() {
      return Instant.now().isAfter(expiresAt);
    }
  }
}
