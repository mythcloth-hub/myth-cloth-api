package com.mesofi.mythclothapi.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class MethodNameJsonProvider implements ArgumentsProvider {

  private static final Path BASE_PATH = Path.of("src/test/resources/payloads");
  final ObjectMapper mapper =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // optional but recommended

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {

    MethodFileSource annotation =
        context.getRequiredTestMethod().getAnnotation(MethodFileSource.class);

    String methodName = context.getRequiredTestMethod().getName();
    String fileName = methodName + ".json";

    Class<?> clazz = annotation.type();
    String folderName = StringUtils.hasText(annotation.folder()) ? annotation.folder() : "";
    folderName = folderName.startsWith("/") ? folderName.replaceFirst("/", "") : folderName;

    // Build folder path safely
    Path filePath =
        folderName.isBlank()
            ? BASE_PATH.resolve(fileName)
            : BASE_PATH.resolve(folderName).resolve(fileName);

    if (!Files.exists(filePath)) {
      throw new IllegalStateException(
          "JSON file not found: " + filePath + " (expected for test method: " + methodName + ")");
    }

    String json = Files.readString(filePath);
    return Stream.of(
        Arguments.of(
            json, clazz.isAssignableFrom(String.class) ? null : mapper.readValue(json, clazz)));
  }
}
