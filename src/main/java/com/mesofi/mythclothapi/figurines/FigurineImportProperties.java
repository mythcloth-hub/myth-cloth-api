package com.mesofi.mythclothapi.figurines;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Externalized settings used to resolve the CSV source for figurine imports.
 *
 * <p>Maps properties under the {@code myth-cloth.import} prefix.
 */
@Data
@ConfigurationProperties(prefix = "myth-cloth.import")
public class FigurineImportProperties {
  /**
   * URL template used to fetch the CSV, expected to contain a {@code %s} placeholder for the
   * configured file id.
   */
  private String driveUrl;

  /** Identifier of the spreadsheet/file to inject into {@link #driveUrl}. */
  private String fileId;

  /**
   * Builds the final CSV URL by formatting {@link #driveUrl} with {@link #fileId}.
   *
   * @return resolved URL pointing to the import CSV source
   */
  public String buildUrl() {
    return driveUrl.formatted(fileId);
  }
}
