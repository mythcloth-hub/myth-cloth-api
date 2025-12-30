package com.mesofi.mythclothapi.it;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for all HTTP-based integration tests.
 *
 * <p>This class bootstraps the full Spring application context using the {@code test} profile and
 * exposes reusable helper methods for creating required domain data (catalogs, distributors,
 * anniversaries, etc.) through real HTTP calls.
 *
 * <p>All controller-level integration tests should extend this class to:
 *
 * <ul>
 *   <li>Ensure consistent environment configuration
 *   <li>Reuse common test setup logic
 *   <li>Validate real API behavior instead of mocking layers
 * </ul>
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

  /** Base endpoint path for figurine-related operations. */
  protected static final String FIGURINES = "/figurines";

  /**
   * REST client bound to the randomly assigned test server port.
   *
   * <p>Used to perform real HTTP requests against the application context.
   */
  @Autowired protected TestRestTemplate rest;
}
