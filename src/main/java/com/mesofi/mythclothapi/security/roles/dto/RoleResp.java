package com.mesofi.mythclothapi.security.roles.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RoleResp(long id, String description) {}
