package com.mesofi.mythclothapi.fix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import com.mesofi.mythclothapi.integration.fix.FxApiClient;
import com.mesofi.mythclothapi.integration.fix.FxRateResponse;

class FxApiClientTest {

  @Test
  void fetchRate_shouldReturnRateWhenPayloadContainsValue() {
    FxApiClient client = new FxApiClient();
    RestClient restClient = mock(RestClient.class);
    RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
    RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    when(restClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri("/api/{from}/{to}.json", "cny", "jpy")).thenReturn(uriSpec);
    when(uriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(FxRateResponse.class))
        .thenReturn(new FxRateResponse("CNY", "JPY", new BigDecimal("20.51"), "2026-05-22"));

    ReflectionTestUtils.setField(client, "restClient", restClient);

    BigDecimal result = client.fetchRate("CNY", "JPY");

    assertThat(result).isEqualByComparingTo("20.51");
  }

  @Test
  void fetchRate_shouldThrowWhenPayloadIsMissingRate() {
    FxApiClient client = new FxApiClient();
    RestClient restClient = mock(RestClient.class);
    RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
    RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    when(restClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri("/api/{from}/{to}.json", "usd", "jpy")).thenReturn(uriSpec);
    when(uriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(FxRateResponse.class))
        .thenReturn(new FxRateResponse("USD", "JPY", null, "2026-05-22"));

    ReflectionTestUtils.setField(client, "restClient", restClient);

    assertThatThrownBy(() -> client.fetchRate("USD", "JPY"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("FX API returned empty rate");
  }
}
