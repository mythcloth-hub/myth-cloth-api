package com.mesofi.mythclothapi.collectors;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FacebookLoginReq {
  @NotNull private String accessToken;
}
