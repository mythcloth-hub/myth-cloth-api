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
  private static final String PERMISSIONS_WRITE_PERMISSION = "permissions:write";
  private static final String PERMISSIONS_READ_PERMISSION = "permissions:read";
  private static final String PERMISSIONS_DELETE_PERMISSION = "permissions:delete";
  private static final String ROLES_WRITE_PERMISSION = "roles:write";
  private static final String ROLES_READ_PERMISSION = "roles:read";
  private static final String ROLES_PERMISSION_ASSIGN_PERMISSION = "roles:permissions:assign";
  private static final String ROLES_PERMISSION_READ_PERMISSION = "roles:permissions:read";
  private static final String ROLES_PERMISSION_SYNC_PERMISSION = "roles:permissions:sync";
  private static final String DISTRIBUTORS_WRITE_PERMISSION = "distributors:write";
  private static final String DISTRIBUTORS_READ_PERMISSION = "distributors:read";
  private static final String DISTRIBUTORS_UPDATE_PERMISSION = "distributors:update";
  private static final String DISTRIBUTORS_DELETE_PERMISSION = "distributors:delete";
  private static final String FIGURINES_WRITE_PERMISSION = "figurines:write";
  private static final String FIGURINES_UPDATE_PERMISSION = "figurines:update";
  private static final String FIGURINES_DELETE_PERMISSION = "figurines:delete";
  private static final String FIGURINES_EVENT_ADD_PERMISSION = "figurines:events:add";
  private static final String FIGURINES_EVENT_READ_PERMISSION = "figurines:events:read";
  private static final String FIGURINES_EVENT_UPDATE_PERMISSION = "figurines:events:update";
  private static final String FIGURINES_EVENT_DELETE_PERMISSION = "figurines:events:delete";
  private static final String FIGURINES_IMAGE_ADD_PERMISSION = "figurines:images:add";
  private static final String FIGURINES_IMAGE_READ_PERMISSION = "figurines:images:read";
  private static final String FIGURINES_IMAGE_DELETE_PERMISSION = "figurines:images:delete";
  private static final String ANNIVERSARIES_WRITE_PERMISSION = "anniversaries:write";

  private static final long TOKEN_EXPIRATION_HOURS =
      720; // valid for 30 days, it's long, but it is OK for testing.

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
                    CATALOG_DELETE_PERMISSION,
                    PERMISSIONS_WRITE_PERMISSION,
                    PERMISSIONS_READ_PERMISSION,
                    PERMISSIONS_DELETE_PERMISSION,
                    ROLES_WRITE_PERMISSION,
                    ROLES_READ_PERMISSION,
                    ROLES_PERMISSION_ASSIGN_PERMISSION,
                    ROLES_PERMISSION_READ_PERMISSION,
                    ROLES_PERMISSION_SYNC_PERMISSION,
                    DISTRIBUTORS_WRITE_PERMISSION,
                    DISTRIBUTORS_READ_PERMISSION,
                    DISTRIBUTORS_UPDATE_PERMISSION,
                    DISTRIBUTORS_DELETE_PERMISSION,
                    FIGURINES_WRITE_PERMISSION,
                    FIGURINES_UPDATE_PERMISSION,
                    FIGURINES_EVENT_ADD_PERMISSION,
                    FIGURINES_EVENT_READ_PERMISSION,
                    FIGURINES_EVENT_UPDATE_PERMISSION,
                    FIGURINES_EVENT_DELETE_PERMISSION,
                    FIGURINES_DELETE_PERMISSION,
                    FIGURINES_IMAGE_ADD_PERMISSION,
                    FIGURINES_IMAGE_READ_PERMISSION,
                    FIGURINES_IMAGE_DELETE_PERMISSION,
                    ANNIVERSARIES_WRITE_PERMISSION))
            .build();

    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }
}
