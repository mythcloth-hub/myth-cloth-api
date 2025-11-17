package com.mesofi.mythclothapi.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Map;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultMatcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProblemDetailAssertions {
  public static ResultMatcher hasTitle(String title) {
    return jsonPath("$.title").value(title);
  }

  public static ResultMatcher hasStatus(int status) {
    return jsonPath("$.status").value(status);
  }

  public static ResultMatcher hasDetail(String detail) {
    return jsonPath("$.detail").value(detail);
  }

  public static ResultMatcher containsDetail(String detail) {
    return jsonPath("$.detail", containsString(detail));
  }

  public static ResultMatcher hasInstance(String instance) {
    return jsonPath("$.instance").value(instance);
  }

  public static ResultMatcher hasTimestamp() {
    return jsonPath("$.timestamp").exists();
  }

  public static ResultMatcher defaultType() {
    return jsonPath("$.type").value("about:blank");
  }

  public static ResultMatcher hasErrors(Map<String, String> errors) {
    return result -> {
      MockHttpServletResponse response = result.getResponse();
      String json = response.getContentAsString();

      // Parse JSON
      JsonNode root = new ObjectMapper().readTree(json);
      JsonNode errorsNode = root.path("errors");

      // Check size
      assertThat(errorsNode.size()).isEqualTo(errors.size());

      // Check each key/value pair
      for (Map.Entry<String, String> entry : errors.entrySet()) {
        assertThat(errorsNode.has(entry.getKey()))
            .as("Expected error key: " + entry.getKey())
            .isTrue();

        assertThat(errorsNode.get(entry.getKey()).asText())
            .as("Expected error message for key " + entry.getKey())
            .isEqualTo(entry.getValue());
      }
    };
  }
}
