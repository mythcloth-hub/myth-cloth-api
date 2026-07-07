package com.mesofi.mythclothapi.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import com.mesofi.mythclothapi.Application;

public final class OpenApiYamlGenerator {

	private OpenApiYamlGenerator() {
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new IllegalArgumentException("Expected output path as the only argument");
		}

		Path output = Path.of(args[0]);
		Files.createDirectories(output.getParent());

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(Application.class)
				.web(WebApplicationType.SERVLET)
				.properties("spring.profiles.active=test", "server.servlet.context-path=/api/v1").run()) {
			OpenApiWebMvcResource resource = context.getBean(OpenApiWebMvcResource.class);
			HttpServletRequest request = createRequest();
			byte[] yaml = resource.openapiYaml(request, "/swagger.yaml", Locale.getDefault());
			Files.write(output, yaml);
		}
	}

	private static HttpServletRequest createRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/swagger.yaml");
		request.setScheme("http");
		request.setServerName("localhost");
		request.setServerPort(9090);
		request.setContextPath("/api/v1");
		request.setRequestURI("/api/v1/swagger.yaml");
		return request;
	}
}
