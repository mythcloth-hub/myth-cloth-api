package com.mesofi.mythclothapi.it;

public record ScenarioContext(String scenarioName, JsonPayload request, ExpectedJson expected) {}
