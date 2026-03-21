package com.mesofi.mythclothapi.it;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a JSON artifact used in a scenario-driven integration test.
 *
 * <p>This annotation is typically used within a {@link FigurineScenario} to declare either:
 *
 * <ul>
 *   <li>a request payload sent to the API, or
 *   <li>an expected response payload used for assertion
 * </ul>
 *
 * <p>The {@code resource} attribute points to a JSON fixture file located under the test resources'
 * directory.
 *
 * <p>The optional {@link CatalogSelector} allows the scenario to specify catalog metadata that will
 * be resolved at runtime and injected into the JSON payload by replacing placeholders (e.g. {@code
 * {{distributionId}}}).
 *
 * <p>The {@code type} attribute determines how the payload is used within the scenario lifecycle.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScenarioRequest {
  /**
   * Optional identifier to reference this request inside a scenario. Empty means "not specified".
   */
  String id() default "";

  /**
   * Name of the JSON fixture file associated with this scenario request.
   *
   * @return fixture file name
   */
  String resource();

  /**
   * Catalog selection criteria used to resolve dynamic catalog identifiers during scenario
   * execution.
   *
   * <p>The selected catalog values are used to replace placeholders found in the associated JSON
   * payload.
   *
   * @return catalog selector metadata
   */
  CatalogSelector catalog() default @CatalogSelector(lineUp = "?", series = "?", group = "?");

  /**
   * Indicates whether this payload represents a request sent to the API or an expected response
   * used for validation.
   *
   * @return payload type
   */
  Type type() default Type.REQUEST;

  /** Payload classification within a scenario. */
  enum Type {
    /** JSON payload used as an HTTP request body. */
    REQUEST,
    /** JSON payload used as the expected HTTP response body. */
    EXPECTED_RESPONSE
  }
}
