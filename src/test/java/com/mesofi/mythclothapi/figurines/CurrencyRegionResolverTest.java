package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.CAD;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CurrencyRegionResolverTest {

  private final CurrencyRegionResolver resolver = new CurrencyRegionResolver();

  @Test
  void resolveCountry_shouldReturnCountryCode_whenCurrencyCodeIsKnown() {
    // Arrange

    // Act
    var countryCode = resolver.resolveCountry(JPY);

    // Assert
    assertThat(countryCode).isEqualTo(JP);
  }

  @Test
  void resolveCountry_shouldThrowException_whenCurrencyCodeIsUnsupported() {
    // Arrange

    // Act + Assert
    assertThatThrownBy(() -> resolver.resolveCountry(CAD))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("No country found for currency")
        .hasMessageContaining("CAD");
  }
}
