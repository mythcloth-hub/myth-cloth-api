package com.mesofi.mythclothapi.figurines;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;

import com.mesofi.mythclothapi.security.config.SecurityConfig;

@WebMvcTest(value = FigurineController.class, properties = {"myth-cloth.security.cors-url=http://localhost:5173",
        "myth-cloth.security.jwt.secret=test-secret-test-secret-test-secret-1234",
        "myth-cloth.security.jwt.issuer=myth-cloth-api", "myth-cloth.security.jwt.ttl-minutes=60"})
@Import(SecurityConfig.class)
class FigurineControllerTest {

}
