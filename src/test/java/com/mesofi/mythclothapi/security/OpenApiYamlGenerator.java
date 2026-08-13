package com.mesofi.mythclothapi.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Map;

import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import com.mesofi.mythclothapi.Application;

public final class OpenApiYamlGenerator {

    private OpenApiYamlGenerator() {
    }

    public static void main(String[] args) throws IOException {
        String output = args.length > 0 ? args[0] : "build/generated/openapi/swagger.yaml";
        Path target = Path.of(output).toAbsolutePath().normalize();

        SpringApplication app = new SpringApplication(Application.class);
        app.setDefaultProperties(Map.of("spring.main.banner-mode", "off", "spring.profiles.active", "test",
                "spring.rabbitmq.listener.simple.auto-startup", "false", "spring.rabbitmq.listener.direct.auto-startup",
                "false"));

        try (ConfigurableApplicationContext context = app.run()) {
            OpenApiWebMvcResource openApiResource = context.getBean(OpenApiWebMvcResource.class);
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/swagger.yaml");
            byte[] yaml = openApiResource.openapiYaml(request, "/swagger.yaml", Locale.ENGLISH);

            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.write(target, yaml, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            System.out.println("OpenAPI YAML generated at " + target);
        }
    }
}
