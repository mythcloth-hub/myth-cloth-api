package com.mesofi.mythclothapi.security.config;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.fasterxml.jackson.core.JsonProcessingException;

@Controller
class OpenApiController {

	private final OpenApiWebMvcResource openApiResource;

	OpenApiController(OpenApiWebMvcResource openApiResource) {
		this.openApiResource = openApiResource;
	}

	@GetMapping(value = "/swagger.yaml", produces = {"application/yaml", "text/yaml", "application/x-yaml"})
	ResponseEntity<byte[]> swaggerYaml(HttpServletRequest request, Locale locale) throws JsonProcessingException {
		byte[] body = openApiResource.openapiYaml(request, "/swagger.yaml", locale);
		return ResponseEntity.ok().contentType(MediaType.valueOf("application/yaml")).body(body);
	}
}
