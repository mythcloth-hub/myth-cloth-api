package com.mesofi.mythclothapi.it;

import static com.mesofi.mythclothapi.distributors.model.CountryCode.CN;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.JP;
import static com.mesofi.mythclothapi.distributors.model.CountryCode.MX;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogType;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.CountryCode;

public class FigurineScenarioExtension
    implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private static final Logger log = LoggerFactory.getLogger(FigurineScenarioExtension.class);

  /**
   * Base directory for JSON fixtures. Can be overridden via JVM property {@code
   * test.fixtures.base}.
   */
  private static final Path BASE_PATH =
      Path.of(
          System.getProperty(
              "test.fixtures.base", "src/integrationTest/resources/payloads/figurines"));

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
  private CatalogTestClient catalogTestClient;
  private RestClient restClient;

  private final List<ScenarioArtifact> payloads = new ArrayList<>();

  /** Shared ObjectMapper configured for test consistency. */
  public static final ObjectMapper mapper =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @Override
  public void beforeEach(ExtensionContext context) {
    this.restClient = retrieveRestClient(context);
    this.catalogTestClient = new CatalogTestClient(restClient);

    FigurineScenario scenario =
        Objects.requireNonNull(
            context.getRequiredTestMethod().getAnnotation(FigurineScenario.class),
            "@FigurineScenario is required");

    String name =
        Optional.of(scenario)
            .filter(s -> StringUtils.hasText(s.name()))
            .map(FigurineScenario::name)
            .orElseThrow(() -> new IllegalArgumentException("Provide a valid scenario name"));

    log.info("Executing scenario '{}' ...", name);
    this.scenarioName = scenario.name();

    CatalogTestClient client = this.catalogTestClient;

    Map<String, Object> placeholders = new HashMap<>();
    for (ScenarioRequest payload : scenario.payloads()) {
      Optional<JsonNode> jsonNode =
          Optional.of(payload)
              .map(ScenarioRequest::resource)
              .filter(StringUtils::hasText)
              .map(filename -> loadJsonFixture(filename, getJsonType(payload.type())));

      if (jsonNode.isEmpty()) {
        continue;
      }

      CatalogSelector selector = payload.catalog();

      boolean hasSupplierId = hasSupplierIdPlaceholder(jsonNode.get());
      boolean hasDistributionId = hasCatalogIdPlaceholder(jsonNode.get(), DIST_ID);
      boolean hasLineUpId = hasCatalogIdPlaceholder(jsonNode.get(), LINEUP_ID);
      boolean hasSeriesId = hasCatalogIdPlaceholder(jsonNode.get(), SERIES_ID);
      boolean hasGroupId = hasCatalogIdPlaceholder(jsonNode.get(), GROUP_ID);
      boolean hasAnniversaryId = hasCatalogIdPlaceholder(jsonNode.get(), ANNIVERSARY_ID);

      if (hasSupplierId) {
        this.distributors =
            Optional.ofNullable(this.distributors).orElseGet(client::createDistributors);

        DistributorResp distributor = findDistributor(this.distributors, JP);
        DistributorResp distributorMXN = findDistributor(this.distributors, MX);
        DistributorResp distributorHK = findDistributor(this.distributors, CN);

        placeholders.put(SUPPLIER_ID, distributor.id());
        placeholders.put(SUPPLIER_ID_MXN, distributorMXN.id());
        placeholders.put(SUPPLIER_ID_HK, distributorHK.id());
      }
      if (hasDistributionId) {
        this.distributions =
            Optional.ofNullable(this.distributions)
                .orElseGet(() -> client.createCatalogs(CatalogType.distributions));

        CatalogResp catalogDistribution =
            findByDescription(
                this.distributions,
                selector.distribution(),
                CatalogResp::description,
                "Distribution not found for a given value");

        placeholders.put(DIST_ID, catalogDistribution.id());
      }
      if (hasLineUpId) {
        this.lineUps =
            Optional.ofNullable(this.lineUps)
                .orElseGet(() -> client.createCatalogs(CatalogType.lineups));

        CatalogResp catalogLineUp =
            findByDescription(
                this.lineUps,
                selector.lineUp(),
                CatalogResp::description,
                "LineUp not found for a given value");

        placeholders.put(LINEUP_ID, catalogLineUp.id());
      }
      if (hasSeriesId) {
        this.series =
            Optional.ofNullable(this.series)
                .orElseGet(() -> client.createCatalogs(CatalogType.series));

        CatalogResp catalogSeries =
            findByDescription(
                this.series,
                selector.series(),
                CatalogResp::description,
                "Series not found for a given value");

        placeholders.put(SERIES_ID, catalogSeries.id());
      }
      if (hasGroupId) {
        this.groups =
            Optional.ofNullable(this.groups)
                .orElseGet(() -> client.createCatalogs(CatalogType.groups));

        CatalogResp catalogGroup =
            findByDescription(
                this.groups,
                selector.group(),
                CatalogResp::description,
                "Group not found for a given value");

        placeholders.put(GROUP_ID, catalogGroup.id());
      }
      if (hasAnniversaryId) {
        this.anniversaries =
            Optional.ofNullable(this.anniversaries).orElseGet(client::createAnniversaries);

        AnniversaryResp catalogAnniversary =
            this.anniversaries.stream()
                .filter(a -> a.year() == selector.anniversary())
                .findFirst()
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            "Anniversary not found: " + selector.anniversary()));

        placeholders.put(ANNIVERSARY_ID, catalogAnniversary.id());
      }

      if (hasSupplierId
          || hasDistributionId
          || hasLineUpId
          || hasSeriesId
          || hasGroupId
          || hasAnniversaryId) {
        replacePlaceholders(jsonNode.get(), placeholders);
      }

      this.payloads.add(new ScenarioArtifact(payload.id(), jsonNode.get(), payload.type()));
    }
  }

  private RestClient retrieveRestClient(ExtensionContext context) {
    // Get Spring context
    Environment environment = SpringExtension.getApplicationContext(context).getEnvironment();

    // Get random port
    Integer port = environment.getProperty("local.server.port", Integer.class);
    String contextPath = environment.getProperty("server.servlet.context-path", String.class);

    if (port == null) {
      throw new IllegalStateException("local.server.port not available");
    }

    // Build RestClient
    return RestClient.builder().baseUrl("http://localhost:" + port + contextPath).build();
  }

  private boolean hasSupplierIdPlaceholder(JsonNode node) {
    return hasPlaceholder(node, SUPPLIER_ID_PLACEHOLDER);
  }

  private boolean hasCatalogIdPlaceholder(JsonNode node, String catalogId) {
    Pattern pattern = Pattern.compile("\\{\\{" + Pattern.quote(catalogId) + "}}");
    return hasPlaceholder(node, pattern);
  }

  private boolean hasPlaceholder(JsonNode node, Pattern pattern) {
    if (node.isTextual()) {
      return pattern.matcher(node.asText()).matches();
    }

    if (node.isContainerNode()) {
      for (JsonNode child : node) {
        if (hasPlaceholder(child, pattern)) {
          return true;
        }
      }
    }
    return false;
  }

  private JsonFixtureType getJsonType(ScenarioRequest.Type type) {
    return type == ScenarioRequest.Type.EXPECTED_RESPONSE
        ? JsonFixtureType.RESPONSE
        : JsonFixtureType.REQUEST;
  }

  private JsonNode loadJsonFixture(String filename, JsonFixtureType fixtureType) {
    Path filePath = BASE_PATH.resolve(fixtureType.folder()).resolve(filename);

    if (!Files.exists(filePath)) {
      throw new IllegalStateException("JSON fixture not found: " + filePath);
    }

    try {
      return mapper.readTree(Files.readString(filePath));
    } catch (IOException e) {
      throw new IllegalStateException("Unable to parse JSON file", e);
    }
  }

  private void replacePlaceholders(JsonNode node, Map<String, Object> values) {
    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;

      objectNode
          .properties()
          .forEach(
              entry -> {
                String fieldName = entry.getKey();
                JsonNode child = entry.getValue();

                if (child.isTextual()) {
                  String text = child.asText();

                  if (text.startsWith("{{") && text.endsWith("}}")) {
                    String key = text.substring(2, text.length() - 2);
                    Object replacement = values.get(key);
                    JsonNode replacementJsonNode = mapper.valueToTree(replacement);
                    if (replacement != null) {
                      objectNode.set(fieldName, replacementJsonNode);
                    }
                  }
                } else {
                  replacePlaceholders(child, values);
                }
              });
    }

    if (node.isArray()) {
      node.forEach(item -> replacePlaceholders(item, values));
    }
  }

  private DistributorResp findDistributor(List<DistributorResp> distributors, CountryCode code) {

    return distributors.stream()
        .filter(d -> d.countryCode().equals(code.name()))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException(String.format("%s Distributor not found", code)));
  }

  private <T> T findByDescription(
      List<T> list, String value, Function<T, String> extractor, String errorMessage) {

    return list.stream()
        .filter(e -> extractor.apply(e).equals(value))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format(
                        "%s: '%s'. Available values: %s",
                        errorMessage, value, list.stream().map(extractor).toList())));
  }

  @Override
  public void afterEach(ExtensionContext context) {
    CatalogTestClient client = this.catalogTestClient;

    safeDelete("Distributors", this.distributors, DistributorResp::id, client::deleteDistributor);
    safeDelete(
        "Distribution",
        this.distributions,
        CatalogResp::id,
        id -> client.deleteCatalog(CatalogType.distributions, id));
    safeDelete(
        "Lineups",
        this.lineUps,
        CatalogResp::id,
        id -> client.deleteCatalog(CatalogType.lineups, id));
    safeDelete(
        "Series", this.series, CatalogResp::id, id -> client.deleteCatalog(CatalogType.series, id));
    safeDelete(
        "Groups", this.groups, CatalogResp::id, id -> client.deleteCatalog(CatalogType.groups, id));

    safeDelete("Anniversaries", this.anniversaries, AnniversaryResp::id, client::deleteAnniversary);

    // initialize the catalogs.
    this.distributors = null;
    this.distributions = null;
    this.lineUps = null;
    this.series = null;
    this.groups = null;
    this.anniversaries = null;

    // removes the payloads too.
    this.payloads.clear();

    log.info("Finished scenario '{}'", this.scenarioName);
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType().equals(FigurineScenarioContext.class);
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {

    return new FigurineScenarioContext(this.scenarioName, this.payloads, this.restClient);
  }

  private <T> void safeDelete(
      String label, List<T> items, Function<T, Long> extractor, Consumer<Long> deleter) {

    if (Objects.isNull(items)) {
      return;
    }
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
}
