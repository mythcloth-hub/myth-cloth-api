package com.mesofi.mythclothapi.it;

import java.util.List;

import org.springframework.web.client.RestClient;

/**
 * Context object provided to scenario-driven integration tests.
 *
 * <p>{@code FigurineScenarioContext} contains all information resolved during scenario setup,
 * including:
 *
 * <ul>
 *   <li>The scenario name, used for logging and traceability
 *   <li>The list of resolved {@link ScenarioArtifact}s associated with the scenario
 * </ul>
 *
 * <p>This context is created by the scenario extension and injected into test methods, allowing
 * tests to access request and expected response payloads without being coupled to fixture-loading
 * or placeholder-resolution logic.
 *
 * @param name scenario name
 * @param payloads resolved scenario payloads
 */
public record FigurineScenarioContext(
    String name, List<ScenarioArtifact> payloads, RestClient restClient) {}
