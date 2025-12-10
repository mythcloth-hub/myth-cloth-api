package com.mesofi.mythclothapi.utils;

import java.io.IOException;

public final class JsonUtils {

  private JsonUtils() {}

  public static String read(String classpath) {
    try (var is = JsonUtils.class.getClassLoader().getResourceAsStream(classpath)) {
      if (is == null) throw new IllegalArgumentException("File not found: " + classpath);
      return new String(is.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException("Unable to read JSON: " + classpath, e);
    }
  }
}
