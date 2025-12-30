package com.mesofi.mythclothapi.it;

import com.fasterxml.jackson.databind.JsonNode;

public record ExpectedJson(String raw, JsonNode json) implements JsonWrapper {}
