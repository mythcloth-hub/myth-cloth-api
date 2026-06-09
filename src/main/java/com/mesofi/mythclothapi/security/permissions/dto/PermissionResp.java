package com.mesofi.mythclothapi.security.permissions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PermissionResp(long id, String description) {}
