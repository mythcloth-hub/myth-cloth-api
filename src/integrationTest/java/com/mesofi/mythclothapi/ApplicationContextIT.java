package com.mesofi.mythclothapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to verify that the application context starts successfully
 * with the "integration" profile.
 */
@SpringBootTest
@ActiveProfiles("integration")
class ApplicationContextIT {

    /**
     * Test to ensure that the application context loads without any issues when the
     * "integration" profile is active.
     */
    @Test
    void shouldStartApplicationWithIntegrationProfile() {
    }
}
