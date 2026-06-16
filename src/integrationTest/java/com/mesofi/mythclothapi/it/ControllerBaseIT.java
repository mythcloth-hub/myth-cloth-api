package com.mesofi.mythclothapi.it;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import com.mesofi.mythclothapi.utils.TestRestClientFactory;

/**
 * Base class for integration tests that execute against the application's embedded HTTP server.
 *
 * <p>This class starts the Spring Boot application using a random available port and configures a
 * {@link RestClient} instance that can be reused by concrete controller integration tests.
 *
 * <p>The configured REST client automatically includes an Authorization header containing a
 * generated JWT token with administrative privileges. This allows integration tests to execute
 * secured endpoints without depending on external authentication providers.
 *
 * <p>Example usage:
 *
 * <pre>
 * public class CatalogControllerIT extends ControllerBaseIT {
 *
 *   &#64;Test
 *   void shouldCreateCatalog() {
 *     rest.post()
 *         .uri("/catalogs/series")
 *         .retrieve();
 *   }
 * }
 * </pre>
 */
@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ControllerBaseIT {

  /** Random port assigned by Spring Boot when starting the embedded server. */
  @LocalServerPort private int port;

  /** Application context path configured for the running test instance. */
  @Value("${server.servlet.context-path}")
  private String contextPath;

  /** JWT encoder used to generate authentication tokens for integration tests. */
  @Autowired private JwtEncoder jwtEncoder;

  /**
   * HTTP client configured with the application base URL and authentication header.
   *
   * <p>Subclasses can use this client to execute HTTP requests against controller endpoints.
   */
  protected RestClient rest;

  /**
   * Initializes the REST client before executing integration tests.
   *
   * <p>A test JWT token is generated and configured as a Bearer token in the default Authorization
   * header. All requests executed using {@link #rest} will be authenticated as an administrator
   * user.
   */
  @BeforeAll
  void setUpRestClient() {
    this.rest = new TestRestClientFactory(jwtEncoder).createAdminClient(port, contextPath);
  }
}
