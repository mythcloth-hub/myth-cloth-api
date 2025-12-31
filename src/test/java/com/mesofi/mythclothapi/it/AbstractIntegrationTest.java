package com.mesofi.mythclothapi.it;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for all HTTP-based integration tests.
 *
 * <p>This class boots the full Spring Boot application using the {@code test} profile and exposes
 * shared infrastructure required by controller-level integration tests.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Start the application on a random HTTP port
 *   <li>Activate the {@code test} Spring profile
 *   <li>Provide a configured {@link TestRestTemplate} for real HTTP calls
 *   <li>Expose common API endpoint constants
 * </ul>
 *
 * <p>All integration tests targeting REST controllers should extend this class to ensure consistent
 * environment setup and realistic end-to-end verification.
 *
 * <p><strong>Design note:</strong> This class intentionally avoids mocking any infrastructure
 * components. Tests extending it validate the complete request lifecycle: controller → service →
 * persistence → response serialization.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

  /**
   * Base endpoint path for figurine-related operations.
   *
   * <p>Exposed as a constant to avoid hard-coded URL strings across test classes.
   */
  protected static final String FIGURINES = "/figurines";

  /**
   * REST client bound to the randomly assigned test server port.
   *
   * <p>This {@link TestRestTemplate} is automatically configured by Spring Boot and targets the
   * embedded server started for the test context.
   *
   * <p>It should be used to perform real HTTP requests (GET, POST, PUT, DELETE) against the
   * application, ensuring full integration coverage.
   */
  @Autowired protected TestRestTemplate rest;
}
