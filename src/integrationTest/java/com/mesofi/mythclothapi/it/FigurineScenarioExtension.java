package com.mesofi.mythclothapi.it;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;

public class FigurineScenarioExtension
    implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

  private static final Logger log = LoggerFactory.getLogger(FigurineScenarioExtension.class);

  /**
   * Base directory for JSON fixtures. Can be overridden via JVM property {@code
   * test.fixtures.base}.
   */
  private static final Path BASE_PATH =
      Path.of(System.getProperty("test.fixtures.base", "src/test/resources/payloads/figurines"));

  private static final Pattern SUPPLIER_ID_PLACEHOLDER =
      Pattern.compile("\\{\\{supplierId([A-Z]+)?}}");

  private static final String SUPPLIER_ID = "supplierId";
  private static final String SUPPLIER_ID_MXN = "supplierIdMXN";
  private static final String SUPPLIER_ID_HK = "supplierIdHK";
  private static final String DIST_ID = "distributionId";
  private static final String LINEUP_ID = "lineUpId";
  private static final String SERIES_ID = "seriesId";
  private static final String GROUP_ID = "groupId";
  private static final String ANNIVERSARY_ID = "anniversaryId";

  List<DistributorResp> distributors;
  List<CatalogResp> distributions;
  List<CatalogResp> lineUps;
  List<CatalogResp> series;
  List<CatalogResp> groups;
  List<AnniversaryResp> anniversaries;

  private String scenarioName;
  private final List<ScenarioArtifact> payloads = new ArrayList<>();

  /** Shared ObjectMapper configured for test consistency. */
  public static final ObjectMapper mapper =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @Override
  public void beforeEach(ExtensionContext context) {}

  @Override
  public void afterEach(ExtensionContext context) {}

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {

    return false;
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return null;
  }
}
