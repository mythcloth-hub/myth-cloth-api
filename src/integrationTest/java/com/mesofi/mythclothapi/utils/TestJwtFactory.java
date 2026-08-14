package com.mesofi.mythclothapi.utils;

import static com.mesofi.mythclothapi.collectorproviders.model.ProviderType.FACEBOOK;
import static com.mesofi.mythclothapi.security.service.SecurityDataService.AVAILABLE_PERMISSIONS;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.mesofi.mythclothapi.security.roles.model.RoleType;

/**
 * Factory used by integration tests to create JWT tokens.
 *
 * <p>
 * This class generates signed JWT tokens that simulate authenticated users
 * during integration tests. The generated tokens contain the roles and
 * permissions required by Spring Security authorization rules.
 *
 * <p>
 * This implementation is thread-safe and can be used when tests execute in
 * parallel because it does not keep mutable static state.
 */
public final class TestJwtFactory {

    private static final String TEST_ISSUER = "myth-cloth-api-test";
    private static final String TEST_USER = "123456789"; // arbitrary user ID for testing

    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_EMAIL = "name";
    private static final String CLAIM_PROVIDER_USER_ID = "provider_user_id";
    private static final String CLAIM_PROVIDER = "provider";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";

    private static final String ADMIN_ROLE = RoleType.ADMIN.getDisplayName();

    private static final long TOKEN_EXPIRATION_HOURS = 720; // valid for 30 days, it's long, but it is OK for testing.

    private final JwtEncoder encoder;

    /**
     * Creates a new JWT factory.
     *
     * @param encoder
     *            JWT encoder used to sign generated tokens
     * @throws NullPointerException
     *             if encoder is null
     */
    public TestJwtFactory(JwtEncoder encoder) {
        this.encoder = Objects.requireNonNull(encoder, "JwtEncoder cannot be null");
    }

    /**
     * Creates a JWT token representing an administrator user.
     *
     * <p>
     * The token contains:
     *
     * <ul>
     * <li>ADMIN role
     * <li>catalogs:create permission</n<li>one hour expiration
     * </ul>
     *
     * @return signed JWT token value
     */
    public String createAdminToken() {

        JwtClaimsSet claims = JwtClaimsSet.builder().subject(TEST_USER).issuer(TEST_ISSUER).issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS)).claim(CLAIM_PROVIDER, FACEBOOK)
                .claim(CLAIM_PROVIDER_USER_ID, "102359319715722089") // arbitrary provider user ID for testing
                .claim(CLAIM_NAME, "Test").claim(CLAIM_EMAIL, "test-admin@mesofi.com")
                .claim(CLAIM_ROLES, List.of(ADMIN_ROLE)).claim(CLAIM_PERMISSIONS, AVAILABLE_PERMISSIONS).build();

        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
