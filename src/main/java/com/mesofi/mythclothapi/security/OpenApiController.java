package com.mesofi.mythclothapi.security;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Controller that exposes the application's OpenAPI specification in YAML
 * format.
 *
 * <p>
 * The generated specification is available at {@code /swagger.yaml} and can be
 * used by API documentation tools or clients that consume OpenAPI definitions.
 * </p>
 */
@Controller
public class OpenApiController {

    private final OpenApiWebMvcResource openApiResource;

    /**
     * Creates a new controller using the SpringDoc OpenAPI resource.
     *
     * @param openApiResource
     *            the SpringDoc resource responsible for generating the OpenAPI
     *            specification
     */
    OpenApiController(OpenApiWebMvcResource openApiResource) {
        this.openApiResource = openApiResource;
    }

    /**
     * Returns the application's OpenAPI specification in YAML format.
     *
     * @param request
     *            the current HTTP request
     * @param locale
     *            the locale used when generating the OpenAPI specification
     * @return a response containing the OpenAPI specification as YAML
     * @throws JsonProcessingException
     *             if the OpenAPI specification cannot be serialized
     */
    @GetMapping(value = "/swagger.yaml", produces = {"application/yaml", "text/yaml", "application/x-yaml"})
    ResponseEntity<byte[]> swaggerYaml(HttpServletRequest request, Locale locale) throws JsonProcessingException {

        byte[] body = openApiResource.openapiYaml(request, "/swagger.yaml", locale);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/yaml")).body(body);
    }
}