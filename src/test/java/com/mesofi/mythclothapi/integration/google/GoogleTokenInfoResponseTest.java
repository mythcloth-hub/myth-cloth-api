package com.mesofi.mythclothapi.integration.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class GoogleTokenInfoResponseTest {

  @Test
  void emailVerified_shouldReturnTrue_whenRawValueIsTrue() {
    GoogleTokenInfoResponse response =
        new GoogleTokenInfoResponse(
            "https://accounts.google.com",
            "client-id",
            "sub-1",
            "seiya@example.com",
            "true",
            "Seiya",
            "https://img/seiya.jpg",
            "1735689600");

    assertThat(response.emailVerified()).isTrue();
  }

  @Test
  void emailVerified_shouldReturnFalse_whenRawValueIsNotTrue() {
    GoogleTokenInfoResponse responseWithFalse =
        new GoogleTokenInfoResponse(
            "https://accounts.google.com",
            "client-id",
            "sub-2",
            "shiryu@example.com",
            "false",
            "Shiryu",
            "https://img/shiryu.jpg",
            "1735689601");

    GoogleTokenInfoResponse responseWithNull =
        new GoogleTokenInfoResponse(
            "https://accounts.google.com",
            "client-id",
            "sub-3",
            "hyoga@example.com",
            null,
            "Hyoga",
            "https://img/hyoga.jpg",
            "1735689602");

    assertThat(responseWithFalse.emailVerified()).isFalse();
    assertThat(responseWithNull.emailVerified()).isFalse();
  }

  @Test
  void expiresAtEpochSecond_shouldReturnParsedLong_whenExpIsNumeric() {
    GoogleTokenInfoResponse response =
        new GoogleTokenInfoResponse(
            "https://accounts.google.com",
            "client-id",
            "sub-4",
            "shun@example.com",
            "true",
            "Shun",
            "https://img/shun.jpg",
            "1735689603");

    assertThat(response.expiresAtEpochSecond()).isEqualTo(1735689603L);
  }

  @Test
  void expiresAtEpochSecond_shouldThrowNumberFormatException_whenExpIsInvalid() {
    GoogleTokenInfoResponse response =
        new GoogleTokenInfoResponse(
            "https://accounts.google.com",
            "client-id",
            "sub-5",
            "ikki@example.com",
            "true",
            "Ikki",
            "https://img/ikki.jpg",
            "not-a-number");

    assertThatThrownBy(response::expiresAtEpochSecond).isInstanceOf(NumberFormatException.class);
  }
}
