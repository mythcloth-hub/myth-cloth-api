package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.ANNOUNCEMENT;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.PREORDER_OPEN;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.RELEASE;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.anniversaries.AnniversaryRepository;
import com.mesofi.mythclothapi.catalogs.repository.DistributionRepository;
import com.mesofi.mythclothapi.catalogs.repository.GroupRepository;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.distributors.DistributorRepository;
import com.mesofi.mythclothapi.figurinedistributions.FigurineDistributorRepository;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.mapper.CatalogContext;
import com.mesofi.mythclothapi.figurines.mapper.FigurineCsv;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.opencsv.bean.CsvToBeanBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service layer responsible for managing {@link Figurine} lifecycle operations.
 *
 * <p>This service encapsulates:
 *
 * <ul>
 *   <li>Importing figurines from a public Google Drive CSV file
 *   <li>Creating and updating figurines
 *   <li>Resolving catalog references (series, groups, distributors, etc.)
 *   <li>Creating default timeline events (announcement, preorder, release)
 *   <li>Calculating region-aware prices and taxes
 * </ul>
 *
 * <p>The service acts as the orchestration layer between:
 *
 * <ul>
 *   <li>CSV / API input DTOs
 *   <li>Domain entities
 *   <li>Catalog repositories
 * </ul>
 *
 * <p>All persistence-related operations are transactional to ensure consistency across figurines,
 * distributors, and events.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FigurineService {

  /**
   * Public Google Drive CSV export URL template.
   *
   * <p>The {@code fileId} is injected at runtime.
   */
  private static final String DRIVE_URL =
      "https://docs.google.com/spreadsheets/d/%s/export?format=csv";

  private final FigurineMapper mapper;
  private final DistributorRepository distributorRepository;
  private final DistributionRepository distributionRepository;
  private final LineUpRepository lineUpRepository;
  private final SeriesRepository seriesRepository;
  private final GroupRepository groupRepository;
  private final AnniversaryRepository anniversaryRepository;
  private final FigurineRepository repository;
  private final CurrencyRegionResolver currencyRegionResolver;
  private final FigurineDistributorRepository figurineDistributorRepository;

  /**
   * Imports figurines from a publicly accessible Google Drive CSV file.
   *
   * <p>The import process:
   *
   * <ol>
   *   <li>Loads all catalog data into a {@link CatalogContext}
   *   <li>Parses the CSV file into {@link FigurineCsv} records
   *   <li>Upserts figurines using {@code legacyName} as a unique key
   *   <li>Persists all changes in a single transaction
   * </ol>
   *
   * @param fileId Google Drive file identifier
   * @throws IllegalStateException if the CSV cannot be read or parsed
   */
  @Transactional
  public void importFromPublicDrive(final String fileId) {
    String fileUrl = DRIVE_URL.formatted(fileId);
    CatalogContext catalogContext = loadCatalogs();

    try (Reader reader = new InputStreamReader(URI.create(fileUrl).toURL().openStream())) {

      List<FigurineCsv> csvRows =
          new CsvToBeanBuilder<FigurineCsv>(reader)
              .withType(FigurineCsv.class)
              .withIgnoreLeadingWhiteSpace(true)
              .build()
              .parse();

      List<Figurine> figurines =
          csvRows.stream().map(csv -> upsertFigurine(csv, catalogContext)).toList();

      List<Figurine> saved = repository.saveAllAndFlush(figurines);
      log.info("{} figurines have been processed (inserted or updated)", saved.size());
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to read CSV from Google Drive", ex);
    }
  }

  /**
   * Inserts or updates a {@link Figurine} based on its legacy name.
   *
   * <p>If a figurine already exists:
   *
   * <ul>
   *   <li>Its mutable fields are updated
   *   <li>References and relationships are re-linked
   * </ul>
   *
   * <p>If it does not exist:
   *
   * <ul>
   *   <li>Default events are created
   *   <li>Timestamps are initialized
   * </ul>
   *
   * @param csv CSV row representation
   * @param context preloaded catalog context
   * @return managed {@link Figurine} entity
   */
  private Figurine upsertFigurine(FigurineCsv csv, CatalogContext context) {
    // Convert CSV → Incoming entity
    Figurine incoming = mapper.toFigurine(csv, context);

    // Find existing by unique key (legacyName)
    return repository
        .findByLegacyName(incoming.getLegacyName())
        .map(
            existing -> {
              // Update existing record
              mapper.updateFigurine(existing, incoming);
              linkReferences(existing);
              return existing;
            })
        .orElseGet(
            () -> {
              // Create a new record
              prepareForPersistence(incoming);
              return incoming;
            });
  }

  /**
   * Creates a new {@link Figurine} from an API request.
   *
   * <p>This method:
   *
   * <ul>
   *   <li>Maps the request into a domain entity
   *   <li>Resolves all catalog references
   *   <li>Creates default events and timestamps
   * </ul>
   *
   * @param request validated figurine creation request
   * @return API response DTO for the created figurine
   */
  @Transactional
  public FigurineResp createFigurine(@NotNull @Valid FigurineReq request) {
    log.info("Creating figurine '{}'", request.name());

    CatalogContext catalogContext = loadCatalogs();

    Figurine figurine = mapper.toFigurine(request, catalogContext);
    prepareForPersistence(figurine);

    var saved = repository.save(figurine);
    return mapper.toFigurineResp(saved, this::createDisplayableName, this::calculatePriceWithTax);
  }

  @Transactional
  public FigurineResp updateFigurine(Long id, @Valid FigurineReq request) {
    log.info("Updating figurine with id '{}'. New name: '{}'", id, request.name());
    var existing = repository.findById(id).orElseThrow(() -> new FigurineNotFoundException(id));

    // Ask MapStruct to update fields
    mapper.updateFigurine(existing, mapper.toFigurine(request, loadCatalogs()));

    var updated = repository.save(existing);
    return mapper.toFigurineResp(updated, this::createDisplayableName, this::calculatePriceWithTax);
  }

  /**
   * Builds a human-readable display name for a figurine.
   *
   * @param figurine figurine entity
   * @return displayable name
   */
  public String createDisplayableName(Figurine figurine) {
    return "FIXME";
  }

  /**
   * Calculates the final price including regional taxes based on currency.
   *
   * <p>If price or distributor information is missing, {@code null} is returned.
   *
   * @param figurineDistributor distributor pricing information
   * @return price including applicable tax, or {@code null}
   */
  public Double calculatePriceWithTax(FigurineDistributor figurineDistributor) {
    if (figurineDistributor == null
        || figurineDistributor.getPrice() == null
        || figurineDistributor.getPrice() <= 0) {
      return null;
    }

    return switch (figurineDistributor.getCurrency()) {
      case JPY ->
          calculateJapanesePriceWithTax(
              figurineDistributor.getPrice(), figurineDistributor.getReleaseDate());
      case MXN -> figurineDistributor.getPrice() * 1.16; // example IVA
      case USD -> figurineDistributor.getPrice(); // no VAT by default
      default -> figurineDistributor.getPrice();
    };
  }

  /**
   * Calculates Japanese consumption tax based on historical tax rates.
   *
   * @param price base price
   * @param releaseDate official release date
   * @return price including Japanese tax
   */
  private Double calculateJapanesePriceWithTax(Double price, LocalDate releaseDate) {
    if (releaseDate == null) {
      return price; // fallback: unknown tax date
    }

    double taxRate;

    if (releaseDate.isBefore(LocalDate.of(1997, 4, 1))) {
      taxRate = 0.03;
    } else if (releaseDate.isBefore(LocalDate.of(2014, 4, 1))) {
      taxRate = 0.05;
    } else if (releaseDate.isBefore(LocalDate.of(2019, 10, 1))) {
      taxRate = 0.08;
    } else {
      taxRate = 0.10;
    }

    return price * (1 + taxRate);
  }

  /**
   * Prepares a figurine entity for persistence.
   *
   * <p>This includes:
   *
   * <ul>
   *   <li>Creating default events
   *   <li>Linking bidirectional relationships
   *   <li>Initializing audit timestamps
   * </ul>
   *
   * @param figurine figurine to prepare
   */
  private void prepareForPersistence(Figurine figurine) {
    createDefaultEvents(figurine);
    linkReferences(figurine);

    Instant localDateTime = Instant.now();
    figurine.setCreationDate(localDateTime);
    figurine.setUpdateDate(localDateTime);
  }

  /**
   * Creates default timeline events (announcement, preorder, release) based on distributor-provided
   * dates.
   *
   * @param figurine target figurine
   */
  private void createDefaultEvents(Figurine figurine) {
    // creates the default events ...
    if (figurine.getDistributors() != null && !figurine.getDistributors().isEmpty()) {
      FigurineDistributor figurineDistributor = figurine.getDistributors().getFirst();

      Optional.ofNullable(figurineDistributor.getAnnouncementDate())
          .ifPresent(
              announcementDate ->
                  addDefaultEvent(
                      "First announced as a possible future release.",
                      announcementDate,
                      ANNOUNCEMENT,
                      figurine));
      Optional.ofNullable(figurineDistributor.getPreorderDate())
          .ifPresent(
              preorderDate ->
                  addDefaultEvent(
                      "Pre-orders are officially open.", preorderDate, PREORDER_OPEN, figurine));
      Optional.ofNullable(figurineDistributor.getReleaseDate())
          .ifPresent(
              releaseDate ->
                  addDefaultEvent(
                      "The global release date has been officially announced.",
                      releaseDate,
                      RELEASE,
                      figurine));
    }
  }

  /**
   * Adds a default {@link FigurineEvent} to a figurine.
   *
   * <p>The event region is resolved from the distributor currency.
   *
   * @param description event description
   * @param date event date
   * @param type event type
   * @param figurine target figurine
   */
  private void addDefaultEvent(
      String description, LocalDate date, FigurineEventType type, Figurine figurine) {

    FigurineEvent event = new FigurineEvent();
    event.setDescription(description);
    event.setEventDate(date);
    event.setType(type);
    FigurineDistributor figurineDistributor =
        figurine.getDistributors().stream().findFirst().orElseThrow();
    CurrencyCode currencyCode = figurineDistributor.getCurrency();

    event.setRegion(currencyRegionResolver.resolveCountry(currencyCode));

    figurine.getEvents().add(event);
  }

  /**
   * Ensures all bidirectional relationships are properly linked before persistence.
   *
   * @param figurine target figurine
   */
  private void linkReferences(Figurine figurine) {
    figurine.getDistributors().forEach(d -> d.setFigurine(figurine));
    figurine.getEvents().forEach(e -> e.setFigurine(figurine));
  }

  /**
   * Loads all catalog entities into memory to optimize lookup during import and creation flows.
   *
   * @return populated {@link CatalogContext}
   */
  private CatalogContext loadCatalogs() {
    return new CatalogContext(
        distributorRepository.findAll(),
        distributionRepository.findAll(),
        lineUpRepository.findAll(),
        seriesRepository.findAll(),
        groupRepository.findAll(),
        anniversaryRepository.findAll());
  }
}
