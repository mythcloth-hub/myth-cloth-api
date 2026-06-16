package com.mesofi.mythclothapi.utils;

import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.client.RestClient;

/**
 * Factory responsible for creating configured {@link RestClient} instances for integration tests.
 *
 * <p>This factory creates clients preconfigured with:
 *
 * <ul>
 *   <li>The application's local test server base URL.
 *   <li>A valid JWT Bearer token for authenticated requests.
 *   <li>Default authorization headers required by secured endpoints.
 * </ul>
 *
 * <p>This class is intended to simplify integration tests by avoiding repeated RestClient and JWT
 * setup code.
 */
public class TestRestClientFactory {

  private final JwtEncoder jwtEncoder;

  /**
   * Creates a new {@code TestRestClientFactory}.
   *
   * @param jwtEncoder encoder used to generate JWT tokens for test authentication
   */
  public TestRestClientFactory(JwtEncoder jwtEncoder) {
    this.jwtEncoder = jwtEncoder;
  }

  /**
   * Creates a {@link RestClient} configured with an ADMIN JWT token.
   *
   * <p>The returned client:
   *
   * <ul>
   *   <li>Uses the provided application port and context path as the base URL.
   *   <li>Includes a Bearer token in every request.
   *   <li>Can be used to call secured endpoints requiring administrative privileges.
   * </ul>
   *
   * @param port the dynamically assigned test server port
   * @param contextPath the application context path configured for the test environment
   * @return a configured {@link RestClient} instance ready to execute authenticated requests
   */
  public RestClient createAdminClient(int port, String contextPath) {

    TestJwtFactory jwtFactory = new TestJwtFactory(jwtEncoder);

    String token = jwtFactory.createAdminToken();

    return RestClient.builder()
        .baseUrl("http://localhost:%s%s".formatted(port, contextPath))
        .defaultHeaders(headers -> headers.setBearerAuth(token))
        .build();
  }
}
