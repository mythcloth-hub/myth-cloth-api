package com.mesofi.mythclothapi.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

/**
 * Factory used by integration tests to create JWT tokens.
 *
 * <p>This class generates signed JWT tokens that simulate authenticated users during integration
 * tests. The generated tokens contain the roles and permissions required by Spring Security
 * authorization rules.
 *
 * <p>This implementation is thread-safe and can be used when tests execute in parallel because it
 * does not keep mutable static state.
 */
public final class TestJwtFactory {

  private static final String TEST_ISSUER = "myth-cloth-api-test";
  private static final String TEST_USER = "test-user";

  private static final String CLAIM_ROLES = "roles";
  private static final String CLAIM_PERMISSIONS = "permissions";

  private static final String ADMIN_ROLE = "ADMIN";
  private static final String CATALOG_WRITE_PERMISSION = "catalogs:write";
  private static final String CATALOG_READ_PERMISSION = "catalogs:read";
  private static final String CATALOG_UPDATE_PERMISSION = "catalogs:update";
  private static final String CATALOG_DELETE_PERMISSION = "catalogs:delete";

  private static final long TOKEN_EXPIRATION_HOURS = 1;

  private final JwtEncoder encoder;

  /**
   * Creates a new JWT factory.
   *
   * @param encoder JWT encoder used to sign generated tokens
   * @throws NullPointerException if encoder is null
   */
  public TestJwtFactory(JwtEncoder encoder) {
    this.encoder = Objects.requireNonNull(encoder, "JwtEncoder cannot be null");
  }

  /**
   * Creates a JWT token representing an administrator user.
   *
   * <p>The token contains:
   *
   * <ul>
   *   <li>ADMIN role
   *   <li>catalogs:write permission</n
   *   <li>one hour expiration
   * </ul>
   *
   * @return signed JWT token value
   */
  public String createAdminToken() {

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject(TEST_USER)
            .issuer(TEST_ISSUER)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plus(TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS))
            .claim(CLAIM_ROLES, List.of(ADMIN_ROLE))
            .claim(
                CLAIM_PERMISSIONS,
                List.of(
                    CATALOG_WRITE_PERMISSION,
                    CATALOG_READ_PERMISSION,
                    CATALOG_UPDATE_PERMISSION,
                    CATALOG_DELETE_PERMISSION))
            .build();

    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }
}
