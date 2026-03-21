package com.mesofi.mythclothapi.it;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a scenario-driven integration test for figurine-related endpoints.
 *
 * <p>This annotation is applied to test methods and is processed by {@link
 * FigurineScenarioExtension} to drive scenario execution.
 *
 * <p>A figurine scenario defines:
 *
 * <ul>
 *   <li>A human-readable scenario name for logging and traceability
 *   <li>One or more {@link ScenarioRequest} definitions describing request and expected response
 *       payloads
 * </ul>
 *
 * <p>Each scenario request references a JSON fixture and may declare catalog selection metadata
 * used to dynamically resolve and inject entity identifiers into the payload.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FigurineScenario {
  /**
   * Human-readable name of the scenario.
   *
   * <p>This value is used for logging and test reporting purposes.
   *
   * @return scenario name
   */
  String name();

  /**
   * JSON payloads associated with the scenario.
   *
   * <p>Each payload defines a JSON fixture and its role within the scenario (request or expected
   * response).
   *
   * @return scenario payload definitions
   */
  ScenarioRequest[] payloads() default {};
}
