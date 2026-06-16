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

@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminTokenGeneratorIT {

  private static final Logger log = LoggerFactory.getLogger(AdminTokenGeneratorIT.class);

  @Autowired private JwtEncoder jwtEncoder;

  @Test
  void generateAdminToken() {
    String adminToken = new TestJwtFactory(jwtEncoder).createAdminToken();
    log.info("\n===> DummyTokenForTesting: {}", adminToken);
  }
}
