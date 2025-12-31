package com.mesofi.mythclothapi.it;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.MX;
import static com.mesofi.mythclothapi.it.JsonFixtureType.REQUEST;
import static com.mesofi.mythclothapi.it.JsonFixtureType.RESPONSE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogType;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.CountryCode;

/**
 * JUnit 5 extension that orchestrates end-to-end catalog-driven integration test scenarios.
 *
 * <p>This extension is responsible for:
 *
 * <ul>
 *   <li>Loading request/response JSON fixtures
 *   <li>Creating required catalog and distributor data
 *   <li>Hydrating JSON placeholders with runtime IDs
 *   <li>Injecting a {@link ScenarioContext} into test methods
 *   <li>Cleaning up all created data after test execution
 * </ul>
 *
 * <p>Tests using this extension must be annotated with {@link FigurineScenario}. The annotation
 * acts as the declarative contract for test setup.
 *
 * <p>This extension enforces deterministic integration tests by:
 *
 * <ul>
 *   <li>Failing fast on missing fixtures
 *   <li>Failing fast on unresolved placeholders
 *   <li>Ensuring full data cleanup after each test
 * </ul>
 */
public class CatalogScenarioExtension
    implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

  private static final Logger log = LoggerFactory.getLogger(CatalogScenarioExtension.class);

  /**
   * Base directory for JSON fixtures. Can be overridden via JVM property {@code
   * test.fixtures.base}.
   */
  private static final Path BASE_PATH =
      Path.of(System.getProperty("test.fixtures.base", "src/test/resources/payloads/figurines"));

  // ---------------------------------------------------------------------------
  // Scenario state (per test execution)
  // ---------------------------------------------------------------------------

  private List<DistributorResp> distributors;
  private List<CatalogResp> distributions;
  private List<CatalogResp> lineUps;
  private List<CatalogResp> series;
  private List<CatalogResp> groups;
  private List<AnniversaryResp> anniversaries;

  private String scenarioName;
  private JsonPayload jsonPayload;
  private ExpectedJson expectedJson;

  /** Shared ObjectMapper configured for test consistency. */
  public static final ObjectMapper mapper =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  /**
   * Registry of supported JSON wrapper factories.
   *
   * <p>This avoids instanceof checks and allows type-safe wrapper construction.
   */
  private static final Map<
          Class<? extends JsonWrapper>, BiFunction<String, JsonNode, ? extends JsonWrapper>>
      FACTORIES =
          Map.of(
              JsonPayload.class, JsonPayload::new,
              ExpectedJson.class, ExpectedJson::new);

  // ---------------------------------------------------------------------------
  // JUnit lifecycle
  // ---------------------------------------------------------------------------

  /**
   * Prepares the test scenario before execution.
   *
   * <p>This method:
   *
   * <ol>
   *   <li>Reads {@link FigurineScenario} metadata
   *   <li>Loads JSON request and expected response fixtures
   *   <li>Creates all required catalogs and distributors
   *   <li>Resolves runtime IDs
   *   <li>Hydrates placeholders in the request JSON
   * </ol>
   */
  @Override
  public void beforeEach(ExtensionContext context) throws IOException {

    FigurineScenario scenario =
        Objects.requireNonNull(
            context.getRequiredTestMethod().getAnnotation(FigurineScenario.class),
            "@FigurineScenario is required");

    CatalogSelector selector = scenario.catalog();
    ScenarioData data = scenario.data();

    validateSelector(selector);

    log.info("Executing scenario '{}' ...", data.name());
    this.scenarioName = data.name();

    // 1️⃣ Load fixtures
    this.jsonPayload = loadJsonFixture(data.request(), JsonPayload.class, REQUEST);
    this.expectedJson = loadJsonFixture(data.expectedResponse(), ExpectedJson.class, RESPONSE);

    // 2️⃣ Resolve Spring-managed test client
    CatalogTestClient client = retrieveCatalogTestClientFromContext(context);

    // 3️⃣ Create catalogs
    distributors = client.createDistributors();

    distributions =
        StringUtils.hasText(selector.distribution())
            ? client.createCatalogs(CatalogType.distributions)
            : List.of();
    lineUps = client.createCatalogs(CatalogType.lineups);
    series = client.createCatalogs(CatalogType.series);
    groups = client.createCatalogs(CatalogType.groups);
    anniversaries = selector.anniversary() != 0 ? client.createAnniversaries() : List.of();

    DistributorResp distributor = findJapaneseDistributor(distributors, JP);
    DistributorResp distributorMXN = findJapaneseDistributor(distributors, MX);

    CatalogResp catalogDistribution =
        distributions.isEmpty()
            ? null
            : findByDescription(
                distributions,
                selector.distribution(),
                CatalogResp::description,
                "Distribution not found");

    CatalogResp catalogLineUp =
        findByDescription(lineUps, selector.lineUp(), CatalogResp::description, "LineUp not found");

    CatalogResp catalogSeries =
        findByDescription(series, selector.series(), CatalogResp::description, "Series not found");

    CatalogResp catalogGroup =
        findByDescription(groups, selector.group(), CatalogResp::description, "Group not found");

    AnniversaryResp catalogAnniversary =
        anniversaries.isEmpty()
            ? null
            : anniversaries.stream()
                .filter(a -> a.year() == selector.anniversary())
                .findFirst()
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            "Anniversary not found: " + selector.anniversary()));

    // 4️⃣ Hydrate placeholders
    Map<String, Object> placeholders =
        Map.of(
            "supplierId",
            distributor.id(),
            "supplierIdMXN",
            Objects.isNull(distributorMXN) ? "" : distributorMXN.id(),
            "distributionId",
            Objects.isNull(catalogDistribution) ? "" : catalogDistribution.id(),
            "lineUpId",
            catalogLineUp.id(),
            "seriesId",
            catalogSeries.id(),
            "groupId",
            catalogGroup.id(),
            "anniversaryId",
            Objects.isNull(catalogAnniversary) ? "" : catalogAnniversary.id());

    this.jsonPayload = hydrate(this.jsonPayload, JsonPayload.class, placeholders);
  }

  // ---------------------------------------------------------------------------
  // Fixture loading
  // ---------------------------------------------------------------------------

  /**
   * Loads a JSON fixture from disk and wraps it in a strongly typed wrapper.
   *
   * @throws IllegalStateException if the fixture file does not exist
   */
  private <T extends JsonWrapper> T loadJsonFixture(
      String filename, Class<T> type, JsonFixtureType fixtureType) throws IOException {

    Path filePath = BASE_PATH.resolve(fixtureType.folder()).resolve(filename);

    if (!Files.exists(filePath)) {
      throw new IllegalStateException("JSON fixture not found: " + filePath);
    }

    String raw = Files.readString(filePath);
    JsonNode json = mapper.readTree(raw);

    var factory = FACTORIES.get(type);
    if (factory == null) {
      throw new IllegalArgumentException("Unsupported JSON wrapper type: " + type.getName());
    }

    return type.cast(factory.apply(raw, json));
  }

  // ---------------------------------------------------------------------------
  // Hydration
  // ---------------------------------------------------------------------------

  /**
   * Replaces placeholder tokens in JSON with resolved runtime values.
   *
   * <p>Placeholders follow the format {@code {{placeholderName}}}. The method fails fast if any
   * placeholder remains unresolved.
   */
  private <T extends JsonWrapper> T hydrate(T wrapper, Class<T> type, Map<String, Object> values)
      throws IOException {

    String hydrated = wrapper.raw();

    for (var entry : values.entrySet()) {
      hydrated =
          hydrated.replaceAll(
              "\\{\\{\\s*" + entry.getKey() + "\\s*}}", Objects.toString(entry.getValue()));
    }

    if (hydrated.matches("(?s).*\\{\\{.*}}.*")) {
      throw new IllegalStateException("Unresolved placeholders found in JSON:\n" + hydrated);
    }

    JsonNode json = mapper.readTree(hydrated);
    return type.cast(FACTORIES.get(type).apply(hydrated, json));
  }

  // ---------------------------------------------------------------------------
  // Lookup helpers
  // ---------------------------------------------------------------------------

  private DistributorResp findJapaneseDistributor(
      List<DistributorResp> distributors, CountryCode countryCode) {

    if (countryCode == JP) {
      return distributors.stream()
          .filter(d -> JP.name().equals(d.countryCode()))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException(JP + " Distributor not found"));
    }

    return distributors.stream()
        .filter(d -> d.countryCode().equals(countryCode.name()))
        .findFirst()
        .orElse(null);
  }

  private <T> T findByDescription(
      List<T> list, String value, Function<T, String> extractor, String errorMessage) {

    return list.stream()
        .filter(e -> extractor.apply(e).equals(value))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(errorMessage + ": " + value));
  }

  private void validateSelector(CatalogSelector selector) {
    if (selector.lineUp().isBlank() || selector.series().isBlank() || selector.group().isBlank()) {
      throw new IllegalStateException("CatalogSelector values must not be blank");
    }
  }

  // ---------------------------------------------------------------------------
  // ParameterResolver
  // ---------------------------------------------------------------------------

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {

    return parameterContext.getParameter().getType().equals(ScenarioContext.class);
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {

    return new ScenarioContext(scenarioName, jsonPayload, expectedJson);
  }

  // ---------------------------------------------------------------------------
  // Cleanup
  // ---------------------------------------------------------------------------

  /**
   * Cleans up all data created for the scenario.
   *
   * <p>Deletion is best-effort; failures are logged but do not fail the test.
   */
  @Override
  public void afterEach(ExtensionContext context) {

    CatalogTestClient client = retrieveCatalogTestClientFromContext(context);

    safeDelete("Distributors", distributors, DistributorResp::id, client::deleteDistributor);
    safeDelete(
        "Distribution",
        distributions,
        CatalogResp::id,
        id -> client.deleteCatalog(CatalogType.distributions, id));
    safeDelete(
        "Lineups", lineUps, CatalogResp::id, id -> client.deleteCatalog(CatalogType.lineups, id));
    safeDelete(
        "Series", series, CatalogResp::id, id -> client.deleteCatalog(CatalogType.series, id));
    safeDelete(
        "Groups", groups, CatalogResp::id, id -> client.deleteCatalog(CatalogType.groups, id));

    safeDelete("Anniversaries", anniversaries, AnniversaryResp::id, client::deleteAnniversary);
  }

  /** Deletes entities in a defensive manner and logs failures instead of failing tests. */
  private <T> void safeDelete(
      String label, List<T> items, Function<T, Long> extractor, Consumer<Long> deleter) {

    List<Long> ids =
        items.stream()
            .map(extractor)
            .peek(
                id -> {
                  try {
                    deleter.accept(id);
                  } catch (Exception ex) {
                    log.warn("Failed to delete {} with id {}", label, id, ex);
                  }
                })
            .toList();

    log.info("Removed {}: {}", label, ids);
  }

  private CatalogTestClient retrieveCatalogTestClientFromContext(ExtensionContext context) {
    return SpringExtension.getApplicationContext(context).getBean(CatalogTestClient.class);
  }
}
