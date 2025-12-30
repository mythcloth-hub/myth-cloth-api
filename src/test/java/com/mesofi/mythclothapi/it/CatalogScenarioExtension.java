package com.mesofi.mythclothapi.it;

import static com.mesofi.mythclothapi.it.JsonFixtureType.REQUEST;
import static com.mesofi.mythclothapi.it.JsonFixtureType.RESPONSE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogType;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;

public class CatalogScenarioExtension implements BeforeEachCallback, ParameterResolver {

  private static final Logger log = LoggerFactory.getLogger(CatalogScenarioExtension.class);

  private static final Path BASE_PATH = Path.of("src/test/resources/payloads/figurines");

  private String scenarioName;
  private JsonPayload jsonPayload;
  private ExpectedJson expectedJson;

  public static final ObjectMapper mapper =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  /** Registry of supported JSON wrapper factories */
  private static final Map<
          Class<? extends JsonWrapper>, BiFunction<String, JsonNode, ? extends JsonWrapper>>
      FACTORIES =
          Map.of(
              JsonPayload.class, JsonPayload::new,
              ExpectedJson.class, ExpectedJson::new);

  // ---------------------------------------------------------------------------
  // JUnit lifecycle
  // ---------------------------------------------------------------------------

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

    this.expectedJson = loadJsonFixture(data.request(), ExpectedJson.class, RESPONSE);

    // 2️⃣ Resolve Spring beans
    var applicationContext = SpringExtension.getApplicationContext(context);

    CatalogTestClient client = applicationContext.getBean(CatalogTestClient.class);

    // 3️⃣ Create catalogs
    List<DistributorResp> distributors = client.createDistributors();
    List<CatalogResp> lineUps = client.createCatalogs(CatalogType.lineups);
    List<CatalogResp> series = client.createCatalogs(CatalogType.series);
    List<CatalogResp> groups = client.createCatalogs(CatalogType.groups);

    DistributorResp distributor = findJapaneseDistributor(distributors);

    CatalogResp catalogLineUp =
        findByDescription(lineUps, selector.lineUp(), CatalogResp::description, "LineUp not found");

    CatalogResp catalogSeries =
        findByDescription(series, selector.series(), CatalogResp::description, "Series not found");

    CatalogResp catalogGroup =
        findByDescription(groups, selector.group(), CatalogResp::description, "Group not found");

    // 4️⃣ Hydrate placeholders
    Map<String, Object> placeholders =
        Map.of(
            "supplierId", distributor.id(),
            "lineUpId", catalogLineUp.id(),
            "seriesId", catalogSeries.id(),
            "groupId", catalogGroup.id());

    this.jsonPayload = hydrate(this.jsonPayload, JsonPayload.class, placeholders);
  }

  // ---------------------------------------------------------------------------
  // Fixture loading
  // ---------------------------------------------------------------------------

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
  // Hydration (warning-free)
  // ---------------------------------------------------------------------------

  private <T extends JsonWrapper> T hydrate(T wrapper, Class<T> type, Map<String, Object> values)
      throws IOException {

    String hydrated = wrapper.raw();

    for (var entry : values.entrySet()) {
      hydrated = hydrated.replace(placeholder(entry.getKey()), Objects.toString(entry.getValue()));
    }

    if (hydrated.contains("{{")) {
      throw new IllegalStateException("Unresolved placeholders found in JSON:\n" + hydrated);
    }

    JsonNode json = mapper.readTree(hydrated);
    return type.cast(FACTORIES.get(type).apply(hydrated, json));
  }

  private static String placeholder(String key) {
    return "{{" + key + "}}";
  }

  // ---------------------------------------------------------------------------
  // Lookup helpers
  // ---------------------------------------------------------------------------

  private static DistributorResp findJapaneseDistributor(List<DistributorResp> distributors) {

    return distributors.stream()
        .filter(d -> "JP".equals(d.countryCode()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("JP Distributor not found"));
  }

  private static <T> T findByDescription(
      List<T> list, String value, Function<T, String> extractor, String errorMessage) {

    return list.stream()
        .filter(e -> extractor.apply(e).equals(value))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(errorMessage + ": " + value));
  }

  private static void validateSelector(CatalogSelector selector) {
    if (selector.lineUp().isBlank() || selector.series().isBlank() || selector.group().isBlank()) {
      throw new IllegalStateException("CatalogSelector values must not be blank");
    }
  }

  // ---------------------------------------------------------------------------
  // ParameterResolver
  // ---------------------------------------------------------------------------

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {

    return parameterContext.getParameter().getType().equals(ScenarioContext.class);
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {

    return new ScenarioContext(scenarioName, jsonPayload, expectedJson);
  }
}
