package com.mesofi.mythclothapi.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mesofi.mythclothapi.security.config.SecurityConfig;

@Import(SecurityConfig.class)
@WebMvcTest(value = OpenApiController.class)
public class OpenApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpenApiWebMvcResource openApiResource;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void swaggerYaml_shouldReturn200AndOpenApiYaml() throws Exception {
        // Arrange
        byte[] yaml = "openapi: 3.0.0\ninfo:\n  title: Myth Cloth API\n".getBytes(StandardCharsets.UTF_8);
        when(openApiResource.openapiYaml(any(HttpServletRequest.class), eq("/swagger.yaml"), any(Locale.class)))
                .thenReturn(yaml);

        // Act & Assert
        mockMvc.perform(get("/swagger.yaml").with(jwt())).andExpect(status().isOk())
                .andExpect(content().contentType("application/yaml")).andExpect(content().bytes(yaml));

        verify(openApiResource).openapiYaml(any(HttpServletRequest.class), eq("/swagger.yaml"), any(Locale.class));
    }
}
