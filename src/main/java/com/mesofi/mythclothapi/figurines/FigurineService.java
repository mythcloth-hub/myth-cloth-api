package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.ANNOUNCEMENT;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.PREORDER_OPEN;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.RELEASE;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.ANNOUNCED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.PROTOTYPE;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.RELEASED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.RUMORED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.UNRELEASED;

import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.anniversaries.AnniversaryRepository;
import com.mesofi.mythclothapi.catalogs.repository.DistributionRepository;
import com.mesofi.mythclothapi.catalogs.repository.GroupRepository;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.catalogs.repository.SeriesRepository;
import com.mesofi.mythclothapi.distributors.DistributorRepository;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.imports.FigurineCsvSource;
import com.mesofi.mythclothapi.figurines.mapper.CatalogContext;
import com.mesofi.mythclothapi.figurines.mapper.FigurineCsv;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.opencsv.bean.CsvToBeanBuilder;

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

  private final FigurineMapper mapper;
  private final FigurineCsvSource csvSource;

  private final DistributorRepository distributorRepository;
  private final DistributionRepository distributionRepository;
  private final LineUpRepository lineUpRepository;
  private final SeriesRepository seriesRepository;
  private final GroupRepository groupRepository;
  private final AnniversaryRepository anniversaryRepository;
  private final FigurineRepository repository;
  private final CurrencyRegionResolver currencyRegionResolver;

  @Transactional
  public void importFromPublicDrive() {
    CatalogContext catalogContext = loadCatalogs();

    try (Reader reader = csvSource.openReader()) {
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
    return mapper.toFigurineResp(
        saved,
        this::createDisplayableName,
        this::calculatePriceWithTax,
        this::calculateReleaseStatus);
  }

  /**
   * Retrieves an existing {@link Figurine} by its identifier.
   *
   * <p>This method:
   *
   * <ul>
   *   <li>Retrieves the figurine by its id
   *   <li>Ensures the figurine exists before mapping
   *   <li>Maps the entity to an API response DTO
   * </ul>
   *
   * <p>The operation is executed in a read-only transactional context and includes derived fields
   * such as display name and region-aware pricing.
   *
   * @param id identifier of the figurine to retrieve
   * @return API response DTO representing the requested figurine
   * @throws FigurineNotFoundException if no figurine exists with the given id
   */
  @Transactional(readOnly = true)
  public FigurineResp readFigurine(Long id) {
    log.info("Reading figurine with id '{}'", id);

    var existing = repository.findById(id).orElseThrow(() -> new FigurineNotFoundException(id));
    return mapper.toFigurineResp(
        existing,
        this::createDisplayableName,
        this::calculatePriceWithTax,
        this::calculateReleaseStatus);
  }

  /**
   * Retrieves a paginated list of figurines.
   *
   * <p>This method:
   *
   * <ul>
   *   <li>Retrieves figurines using Spring Data pagination
   *   <li>Executes in a read-only transactional context
   *   <li>Maps domain entities to API response DTOs
   *   <li>Includes derived fields such as display name and region-aware pricing
   * </ul>
   *
   * @param page zero-based page index
   * @param size number of records per page
   * @return a {@link Page} containing {@link FigurineResp} for the requested slice
   */
  @Transactional(readOnly = true)
  public Page<FigurineResp> readFigurines(int page, int size) {
    log.info("Reading figurines with page '{}' and size '{}'", page, size);

    return repository
        .findAll(PageRequest.of(page, size))
        .map(
            figurine ->
                mapper.toFigurineResp(
                    figurine,
                    this::createDisplayableName,
                    this::calculatePriceWithTax,
                    this::calculateReleaseStatus));
  }

  /**
   * Updates an existing {@link Figurine} with new data provided via an API request.
   *
   * <p>This method:
   *
   * <ul>
   *   <li>Retrieves the existing figurine by its identifier
   *   <li>Maps mutable fields from the request onto the existing entity
   *   <li>Resolves and re-links catalog references as needed
   *   <li>Persists the updated entity within a transactional boundary
   * </ul>
   *
   * <p>Fields not present in the request are preserved according to the mapper configuration.
   *
   * @param id identifier of the figurine to update
   * @param request validated figurine update request
   * @return API response DTO representing the updated figurine
   * @throws FigurineNotFoundException if no figurine exists with the given id
   */
  @Transactional
  public FigurineResp updateFigurine(Long id, @Valid FigurineReq request) {
    log.info("Updating figurine with id '{}'. New name: '{}'", id, request.name());
    var existing = repository.findById(id).orElseThrow(() -> new FigurineNotFoundException(id));

    // Ask MapStruct to update fields
    Figurine incoming = mapper.toFigurine(request, loadCatalogs());
    mapper.updateFigurine(existing, incoming);

    // update the distributors' info.
    updateDistributors(existing, existing.getDistributors(), incoming.getDistributors());

    var updated = repository.save(existing);
    return mapper.toFigurineResp(
        updated,
        this::createDisplayableName,
        this::calculatePriceWithTax,
        this::calculateReleaseStatus);
  }

  /**
   * Deletes an existing {@link Figurine} by its identifier.
   *
   * <p>This method:
   *
   * <ul>
   *   <li>Retrieves the figurine by its id
   *   <li>Ensures the figurine exists before deletion
   *   <li>Removes the figurine from persistence
   * </ul>
   *
   * <p>The operation is logged for traceability. Any associated relationships are handled according
   * to the configured JPA cascade rules.
   *
   * @param id identifier of the figurine to delete
   * @throws FigurineNotFoundException if no figurine exists with the given id
   */
  @Transactional
  public void deleteFigurine(Long id) {
    log.info("Deleting figurine with id '{}'", id);
    var existing = repository.findById(id).orElseThrow(() -> new FigurineNotFoundException(id));

    repository.delete(existing);
  }

  /**
   * Synchronizes distributor entries of a figurine using incoming distributor data.
   *
   * <p>This method performs a currency-based merge between existing and incoming {@link
   * FigurineDistributor} entries:
   *
   * <ul>
   *   <li>If a distributor with the same {@link CurrencyCode} already exists, its mutable fields
   *       are updated
   *   <li>If no matching distributor exists, the incoming entry is linked to the figurine and added
   *       to the collection
   * </ul>
   *
   * <p>Distributor identity is determined exclusively by currency. This method * does not handle
   * removal of existing distributors.
   *
   * @param current the owning figurine
   * @param existing current distributor entries associated with the figurine
   * @param incoming distributor entries provided by the update request
   */
  private void updateDistributors(
      Figurine current, List<FigurineDistributor> existing, List<FigurineDistributor> incoming) {

    for (FigurineDistributor incomingFigurineDist : incoming) {
      CurrencyCode incomingCurrency = incomingFigurineDist.getCurrency();

      existing.stream()
          .filter(fd -> fd.getCurrency().equals(incomingCurrency))
          .findFirst()
          .ifPresentOrElse(
              fd -> mapper.updateFigurineDistributor(fd, incomingFigurineDist),
              () -> {
                incomingFigurineDist.setFigurine(current);
                existing.add(incomingFigurineDist);
              });
    }
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
   * Determines the {@link ReleaseStatus} of a figurine based on its distributor data and dates.
   *
   * <p>The status is resolved using the following rules:
   *
   * <ul>
   *   <li>{@link ReleaseStatus#RUMORED} – no Japanese distributor ({@code JPY}) is found
   *   <li>{@link ReleaseStatus#PROTOTYPE} – announced but not yet released, and the announcement is
   *       less than 5 years ago
   *   <li>{@link ReleaseStatus#UNRELEASED} – announced but not yet released, and the announcement
   *       is 5 or more years ago
   *   <li>{@link ReleaseStatus#ANNOUNCED} – has a release date that is in the future (not yet
   *       released)
   *   <li>{@link ReleaseStatus#RELEASED} – has a release date that is today or in the past (already
   *       released)
   * </ul>
   *
   * @param figurine the figurine whose release status is to be determined
   * @return the computed {@link ReleaseStatus}
   */
  public ReleaseStatus calculateReleaseStatus(Figurine figurine) {
    List<FigurineDistributor> figurineDistributors = figurine.getDistributors();
    Optional<FigurineDistributor> jp =
        figurineDistributors.stream().filter(fd -> fd.getCurrency() == JPY).findFirst();

    if (jp.isEmpty()) {
      return RUMORED;
    } else {
      FigurineDistributor fd = jp.get();
      LocalDate relDate = fd.getReleaseDate();
      LocalDate annDate = fd.getAnnouncementDate();

      if (Objects.nonNull(annDate) && Objects.isNull(relDate)) {
        return LocalDate.now().getYear() - annDate.getYear() >= 5 ? UNRELEASED : PROTOTYPE;
      } else {
        return relDate.isAfter(LocalDate.now()) ? ANNOUNCED : RELEASED;
      }
    }
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
    if (figurine.getDistributors().isEmpty()) {
      log.warn(
          "Figurine '{}' has no distributors, skipping default event creation",
          figurine.getLegacyName());
      return;
    }

    FigurineDistributor figurineDistributor = figurine.getDistributors().getFirst();

    Optional.ofNullable(figurineDistributor.getAnnouncementDate())
        .ifPresent(
            announcementDate ->
                addDefaultEvent(
                    "First announced as a possible future release.",
                    announcementDate,
                    true,
                    ANNOUNCEMENT,
                    figurine));
    Optional.ofNullable(figurineDistributor.getPreorderDate())
        .ifPresent(
            preorderDate ->
                addDefaultEvent(
                    "Pre-orders are officially open.",
                    preorderDate,
                    true,
                    PREORDER_OPEN,
                    figurine));
    Optional.ofNullable(figurineDistributor.getReleaseDate())
        .ifPresent(
            releaseDate ->
                addDefaultEvent(
                    "The global release date has been officially announced.",
                    releaseDate,
                    figurineDistributor.isReleaseDateConfirmed(),
                    RELEASE,
                    figurine));
  }

  /**
   * Adds a default {@link FigurineEvent} to a figurine.
   *
   * <p>The event region is resolved from the distributor currency.
   *
   * @param description event description
   * @param date event date
   * @param dateConfirmed whether the event date is confirmed or tentative
   * @param type event type
   * @param figurine target figurine
   */
  private void addDefaultEvent(
      String description,
      LocalDate date,
      boolean dateConfirmed,
      FigurineEventType type,
      Figurine figurine) {

    FigurineEvent event = new FigurineEvent();
    event.setDescription(description);
    event.setEventDate(date);
    event.setEventDateConfirmed(dateConfirmed);
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
