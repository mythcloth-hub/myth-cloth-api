package com.mesofi.mythclothapi.it;

import com.fasterxml.jackson.databind.JsonNode;

public sealed interface JsonWrapper permits JsonPayload, ExpectedJson {

  String raw();

  JsonNode json();
}
