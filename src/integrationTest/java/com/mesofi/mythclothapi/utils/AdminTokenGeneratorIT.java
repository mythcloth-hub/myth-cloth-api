package com.mesofi.mythclothapi.utils;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for generating an admin JWT token using the
 * {@link TestJwtFactory}.
 *
 * <p>
 * This test is designed to run in an integration test context, where the Spring
 * Boot application is started with a random port. It uses the "integration"
 * profile to ensure that the appropriate configuration is loaded for testing
 * purposes.
 *
 * <p>
 * The generated admin token can be used for testing secured endpoints that
 * require admin privileges.
 */
@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminTokenGeneratorIT {

    private static final Logger log = LoggerFactory.getLogger(AdminTokenGeneratorIT.class);

    @Autowired
    private JwtEncoder jwtEncoder;

    /**
     * Generates an admin JWT token and logs it for testing purposes.
     *
     * <p>
     * This test method creates an admin token using the {@link TestJwtFactory} and
     * logs the generated token. The token can be used in subsequent integration
     * tests to authenticate requests to secured endpoints.
     */
    @Test
    void generateAdminToken() {
        String adminToken = new TestJwtFactory(jwtEncoder).createAdminToken();
        log.info("\n===> DummyTokenForTesting: {}", adminToken);
    }
}
