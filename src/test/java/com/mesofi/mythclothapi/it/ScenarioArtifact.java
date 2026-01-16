package com.mesofi.mythclothapi.it;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a resolved JSON artifact used within a scenario-driven integration test.
 *
 * <p>A {@code ScenarioArtifact} encapsulates:
 *
 * <ul>
 *   <li>The JSON payload after all placeholders have been resolved
 *   <li>The role of the payload within the scenario lifecycle (request or expected response)
 * </ul>
 *
 * <p>Instances of this record are created by the scenario extension during test setup and are later
 * consumed by test methods via {@link FigurineScenarioContext}.
 *
 * @param id unique artifact identifier (it could be empty)
 * @param json resolved JSON payload
 * @param type payload classification within the scenario
 */
public record ScenarioArtifact(String id, JsonNode json, ScenarioRequest.Type type) {}
