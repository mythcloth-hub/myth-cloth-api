package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.CN;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.ES;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.MX;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.US;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.CNY;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.EUR;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.MXN;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.USD;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;

@Component
public class CurrencyRegionResolver {

  private static final Map<CountryCode, CurrencyCode> COUNTRY_TO_CURRENCY =
      Map.of(JP, JPY, MX, MXN, ES, EUR, US, USD, CN, CNY);

  private static final Map<CurrencyCode, CountryCode> CURRENCY_TO_COUNTRY =
      COUNTRY_TO_CURRENCY.entrySet().stream()
          .collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey));

  public CountryCode resolveCountry(CurrencyCode currencyCode) {
    return Optional.ofNullable(CURRENCY_TO_COUNTRY.get(currencyCode))
        .orElseThrow(
            () -> new IllegalArgumentException("No country found for currency: " + currencyCode));
  }
}
