package com.mesofi.mythclothapi.fix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mesofi.mythclothapi.integration.fix.CurrencyConversionService;
import com.mesofi.mythclothapi.integration.fix.FxApiClient;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

  @InjectMocks private CurrencyConversionService service;

  @Mock private FxApiClient fxApiClient;

  @Test
  void convert_shouldReturnOriginalAmountWhenCurrenciesMatchIgnoringCase() {
    BigDecimal amount = new BigDecimal("123.45");

    BigDecimal result = service.convert(amount, "jpy", "JPY");

    assertThat(result).isEqualByComparingTo("123.45");
    verify(fxApiClient, never()).fetchRate(anyString(), anyString());
  }

  @Test
  void convert_shouldFetchRateAndRoundToTwoDecimals() {
    when(fxApiClient.fetchRate("USD", "JPY")).thenReturn(new BigDecimal("150.123"));

    BigDecimal result = service.convert(new BigDecimal("10"), "USD", "JPY");

    assertThat(result).isEqualByComparingTo("1501.23");
    verify(fxApiClient).fetchRate("USD", "JPY");
  }

  @Test
  void convert_shouldReuseCachedRateForSamePair() {
    when(fxApiClient.fetchRate("USD", "JPY")).thenReturn(new BigDecimal("150"));

    BigDecimal first = service.convert(new BigDecimal("1"), "USD", "JPY");
    BigDecimal second = service.convert(new BigDecimal("2"), "usd", "jpy");

    assertThat(first).isEqualByComparingTo("150.00");
    assertThat(second).isEqualByComparingTo("300.00");
    verify(fxApiClient, times(1)).fetchRate("USD", "JPY");
  }

  @Test
  void convert_shouldRefreshExpiredCachedRate() throws Exception {
    when(fxApiClient.fetchRate("USD", "JPY")).thenReturn(new BigDecimal("160"));

    ConcurrentMap<String, Object> cache =
        (ConcurrentMap<String, Object>) ReflectionTestUtils.getField(service, "cache");
    cache.put("USD_JPY", expiredCachedRate(new BigDecimal("150")));

    BigDecimal result = service.convert(new BigDecimal("1"), "USD", "JPY");

    assertThat(result).isEqualByComparingTo("160.00");
    verify(fxApiClient, times(1)).fetchRate("USD", "JPY");
  }

  private Object expiredCachedRate(BigDecimal rate) throws Exception {
    Class<?> cachedRateClass =
        Class.forName(
            "com.mesofi.mythclothapi.integration.fix.CurrencyConversionService$CachedRate");
    Constructor<?> constructor =
        cachedRateClass.getDeclaredConstructor(BigDecimal.class, Instant.class);
    constructor.setAccessible(true);
    return constructor.newInstance(rate, Instant.now().minusSeconds(10));
  }
}
