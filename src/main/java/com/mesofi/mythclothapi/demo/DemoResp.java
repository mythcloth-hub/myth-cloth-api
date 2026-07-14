package com.mesofi.mythclothapi.demo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record DemoResp(boolean enabled, String providerUserId, String name, String email) {
}
