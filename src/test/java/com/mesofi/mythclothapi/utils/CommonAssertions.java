package com.mesofi.mythclothapi.utils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.test.web.servlet.ResultMatcher;

public class CommonAssertions {
  public static ResultMatcher hasId(long id) {
    return jsonPath("$.id").value(id);
  }

  public static ResultMatcher hasName(String name) {
    return jsonPath("$.name").value(name);
  }

  public static ResultMatcher hasDescription(String description) {
    return jsonPath("$.description").value(description);
  }
}
