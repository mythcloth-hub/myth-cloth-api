package com.mesofi.mythclothapi.fix;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * HTTP client wrapper for retrieving foreign exchange rates from {@code fxapi.app}.
 *
 * <p>This component exposes a focused API used by the service layer to fetch a conversion rate
 * between two ISO-4217 currency codes.
 */
@Component
public class FxApiClient {

  private final RestClient restClient;

  /** Creates a client configured with the {@code fxapi.app} base URL. */
  public FxApiClient() {
    this.restClient = RestClient.builder().baseUrl("https://fxapi.app").build();
  }

  /**
   * Fetches the latest conversion rate for a currency pair.
   *
   * @param from source currency code (for example, {@code CNY})
   * @param to target currency code (for example, {@code JPY})
   * @return the conversion rate to multiply an amount in {@code from} currency to obtain the amount
   *     in {@code to} currency
   * @throws IllegalStateException if the upstream API responds without a usable rate
   */
  public BigDecimal fetchRate(String from, String to) {

    FxRateResponse resp =
        restClient
            .get()
            .uri("/api/{from}/{to}.json", from.toLowerCase(), to.toLowerCase())
            .retrieve()
            .body(FxRateResponse.class);

    if (resp == null || resp.rate() == null) {
      throw new IllegalStateException("FX API returned empty rate");
    }

    return resp.rate();
  }
}
