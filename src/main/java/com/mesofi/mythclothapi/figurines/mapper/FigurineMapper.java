package com.mesofi.mythclothapi.figurines.mapper;

import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.CNY;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.JPY;
import static com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode.MXN;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.mesofi.mythclothapi.anniversaries.Anniversary;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurines.dto.DistributorReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineDistributorResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * MapStruct mapper responsible for converting between:
 *
 * <ul>
 *   <li>CSV import models → {@link Figurine}
 *   <li>API request DTOs → {@link Figurine}
 *   <li>Domain entities → API response DTOs
 * </ul>
 *
 * <p>This mapper centralizes all transformation rules for figurines, including:
 *
 * <ul>
 *   <li>Catalog lookups (distribution, lineup, series, group, anniversary)
 *   <li>Distributor and pricing normalization
 *   <li>Event parsing
 *   <li>API-specific field naming conversions
 * </ul>
 *
 * <p>{@link CatalogContext} is used extensively to resolve catalog references without performing
 * database access inside the mapper.
 */
@Mapper(componentModel = "spring")
public interface FigurineMapper {

  /** Formatter used to parse event dates coming from CSV or raw strings. */
  DateTimeFormatter EVENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");

  /* ============================
    CSV → Figurine
  ============================ */
  /**
   * Maps a CSV row into a {@link Figurine} entity.
   *
   * <p>This mapping is used during bulk imports. Catalog references are resolved using textual
   * descriptions rather than IDs.
   *
   * @param csv the CSV representation of a figurine
   * @param catalogs catalog context used to resolve reference data
   * @return a new {@link Figurine} entity ready to be persisted
   */
  @Mapping(target = "id", ignore = true) // populated by DB
  @Mapping(target = "legacyName", source = "originalName")
  @Mapping(target = "normalizedName", source = "baseName")
  @Mapping(
      target = "distributors",
      expression = "java(toDistributors(csv, catalogs.distributors()))")
  @Mapping(target = "distribution", source = "distributionString")
  @Mapping(target = "lineup", source = "lineupString")
  @Mapping(target = "series", source = "seriesString")
  @Mapping(target = "group", source = "groupString")
  @Mapping(target = "anniversary", source = "anniversaryNumber")
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  Figurine toFigurine(FigurineCsv csv, @Context CatalogContext catalogs);

  /**
   * Resolves a {@link Distribution} by description.
   *
   * @param desc distribution description
   * @param catalogs catalog context
   * @return matching {@link Distribution} or {@code null} if input is blank
   * @throws IllegalArgumentException if the description does not exist
   */
  default Distribution toDistribution(String desc, @Context CatalogContext catalogs) {
    String msg = "Distribution not found for desc=" + desc;
    return Optional.ofNullable(desc)
        .filter($ -> !desc.isBlank())
        .map($ -> find(catalogs.distributions(), l -> desc.equals(l.getDescription()), msg))
        .orElse(null);
  }

  /**
   * Resolves a {@link LineUp} catalog entry using its textual description.
   *
   * <p>This variant is primarily used during CSV imports where catalog references are provided as
   * human-readable descriptions rather than identifiers.
   *
   * @param desc lineup description
   * @param catalogs catalog context containing cached lineups
   * @return the matching {@link LineUp}, or {@code null} if the description is blank
   * @throws IllegalArgumentException if the description does not exist in the catalog
   */
  default LineUp toLineUp(String desc, @Context CatalogContext catalogs) {
    String msg = "LineUp not found for desc=" + desc;
    return Optional.ofNullable(desc)
        .filter($ -> !desc.isBlank())
        .map($ -> find(catalogs.lineUps(), l -> desc.equals(l.getDescription()), msg))
        .orElse(null);
  }

  /**
   * Resolves a {@link Series} catalog entry using its textual description.
   *
   * @param desc series description
   * @param catalogs catalog context containing cached series
   * @return the matching {@link Series}, or {@code null} if the description is blank
   * @throws IllegalArgumentException if the description does not exist in the catalog
   */
  default Series toSeries(String desc, @Context CatalogContext catalogs) {
    String msg = "Series not found for desc=" + desc;
    return Optional.ofNullable(desc)
        .filter($ -> !desc.isBlank())
        .map($ -> find(catalogs.series(), l -> desc.equals(l.getDescription()), msg))
        .orElse(null);
  }

  /**
   * Resolves a {@link Group} catalog entry using its textual description.
   *
   * @param desc group description
   * @param catalogs catalog context containing cached groups
   * @return the matching {@link Group}, or {@code null} if the description is blank
   * @throws IllegalArgumentException if the description does not exist in the catalog
   */
  default Group toGroup(String desc, @Context CatalogContext catalogs) {
    String msg = "Group not found for desc=" + desc;
    return Optional.ofNullable(desc)
        .filter($ -> !desc.isBlank())
        .map($ -> find(catalogs.groups(), l -> desc.equals(l.getDescription()), msg))
        .orElse(null);
  }

  /**
   * Resolves an {@link Anniversary} catalog entry using its year value.
   *
   * <p>This method is intended for CSV imports where anniversaries are expressed as numeric years
   * instead of catalog identifiers.
   *
   * @param anniversaryNumber anniversary year (e.g. 20, 30)
   * @param catalogs catalog context containing cached anniversaries
   * @return the matching {@link Anniversary}, or {@code null} if none matches
   */
  default Anniversary toAnniversary(Integer anniversaryNumber, @Context CatalogContext catalogs) {
    return catalogs.anniversaries().stream()
        .filter(item -> Objects.nonNull(item.getYear()))
        .filter(item -> item.getYear().equals(anniversaryNumber))
        .findFirst()
        .orElse(null);
  }

  /**
   * Builds the list of {@link FigurineDistributor} entries for a figurine based on CSV pricing and
   * release information.
   *
   * <p>At minimum, a JP/Asia distributor is created. A Mexico distributor is added only if MXN
   * pricing exists.
   *
   * @param csv CSV data
   * @param distributors available distributors
   * @return list of distributor entries
   */
  default List<FigurineDistributor> toDistributors(
      FigurineCsv csv, @Context List<Distributor> distributors) {

    List<FigurineDistributor> distributorList = new ArrayList<>();
    FigurineDistributor jp = new FigurineDistributor();
    Optional<LocalDateConfirmed> opt = Optional.ofNullable(csv.getReleaseJPY());

    // In case the figurine has been released in China or Japan
    jp.setCurrency(csv.isHk() ? CNY : JPY);
    jp.setPrice(csv.getPriceJPY());
    jp.setAnnouncementDate(csv.getAnnouncementJPY());
    jp.setPreorderDate(csv.getPreorderJPY());
    jp.setReleaseDate(opt.map(LocalDateConfirmed::getDate).orElse(null));
    jp.setReleaseDateConfirmed(opt.map(LocalDateConfirmed::isConfirmed).orElse(false));
    jp.setDistributor(findDistributorByCountry(distributors, CountryCode.JP));
    distributorList.add(jp);

    Optional.ofNullable(csv.getPriceMXN())
        .ifPresent(
            price -> {
              FigurineDistributor mx = new FigurineDistributor();
              mx.setCurrency(MXN);
              mx.setPrice(price);
              mx.setPreorderDate(csv.getPreorderMXN());
              mx.setReleaseDate(csv.getReleaseMXN());
              mx.setDistributor(findDistributorByCountry(distributors, CountryCode.MX));
              distributorList.add(mx);
            });

    return distributorList;
  }

  /**
   * Converts a list of raw event strings into {@link FigurineEvent} entities.
   *
   * <p>Each string must follow the format:
   *
   * <pre>
   * M/d/yyyy: Description
   * </pre>
   *
   * @param eventStrings raw event definitions
   * @return parsed event list
   */
  default List<FigurineEvent> toFigurineEvents(List<String> eventStrings) {
    if (eventStrings == null || eventStrings.isEmpty()) {
      return new ArrayList<>();
    }
    return eventStrings.stream()
        .map(this::parseEventString)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /* ============================
  API → Figurine
  ============================ */
  /**
   * Maps an API request into a {@link Figurine} entity.
   *
   * <p>Catalog references are resolved using IDs. Boolean flags are normalized to internal domain
   * fields.
   *
   * @param req API request
   * @param catalogs catalog context
   * @return a new {@link Figurine} entity
   */
  @Mapping(target = "id", ignore = true) // populated by DB
  @Mapping(target = "legacyName", ignore = true) // no needed
  @Mapping(target = "normalizedName", source = "name")
  @Mapping(target = "distribution", source = "distributionId")
  @Mapping(target = "lineup", source = "lineUpId")
  @Mapping(target = "series", source = "seriesId")
  @Mapping(target = "group", source = "groupId")
  @Mapping(target = "anniversary", source = "anniversaryId")
  @Mapping(target = "metalBody", source = "isMetalBody")
  @Mapping(target = "oce", source = "isOriginalColorEdition")
  @Mapping(target = "revival", source = "isRevival")
  @Mapping(target = "plainCloth", source = "isPlainCloth")
  @Mapping(target = "broken", source = "isBattleDamaged")
  @Mapping(target = "golden", source = "isGoldenArmor")
  @Mapping(target = "gold", source = "isGold24kEdition")
  @Mapping(target = "manga", source = "isMangaVersion")
  @Mapping(target = "surplice", ignore = true) // this will be removed in the future
  @Mapping(target = "set", source = "isMultiPack")
  @Mapping(target = "articulable", source = "isArticulable")
  @Mapping(target = "remarks", source = "notes")
  @Mapping(target = "events", ignore = true) // it's ok, here it is not required to have events.
  @Mapping(target = "officialImages", source = "officialImageUrls")
  @Mapping(target = "nonOfficialImages", source = "unofficialImageUrls")
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  Figurine toFigurine(FigurineReq req, @Context CatalogContext catalogs);

  /**
   * Resolves a {@link Distribution} catalog entry by its identifier.
   *
   * <p>This method is used when mapping API requests where catalog references are provided as IDs
   * instead of descriptions.
   *
   * @param id distribution identifier
   * @param catalogs catalog context containing cached distributions
   * @return the matching {@link Distribution}, or {@code null} if the id is {@code null}
   * @throws IllegalArgumentException if the id does not exist in the catalog
   */
  default Distribution toDistribution(Long id, @Context CatalogContext catalogs) {
    String msg = "Distribution not found for id=" + id;
    return Optional.ofNullable(id)
        .map($ -> find(catalogs.distributions(), l -> Objects.equals(l.getId(), id), msg))
        .orElse(null);
  }

  /** Resolves a {@link LineUp} catalog entry by its identifier. */
  default LineUp toLineUp(Long id, @Context CatalogContext catalogs) {
    String msg = "LineUp not found for id=" + id;
    return Optional.ofNullable(id)
        .map($ -> find(catalogs.lineUps(), l -> Objects.equals(l.getId(), id), msg))
        .orElse(null);
  }

  /** Resolves a {@link Series} catalog entry by its identifier. */
  default Series toSeries(Long id, @Context CatalogContext catalogs) {
    String msg = "Series not found for id=" + id;
    return Optional.ofNullable(id)
        .map($ -> find(catalogs.series(), l -> Objects.equals(l.getId(), id), msg))
        .orElse(null);
  }

  /** Resolves a {@link Group} catalog entry by its identifier. */
  default Group toGroup(Long id, @Context CatalogContext catalogs) {
    String msg = "Group not found for id=" + id;
    return Optional.ofNullable(id)
        .map($ -> find(catalogs.groups(), l -> Objects.equals(l.getId(), id), msg))
        .orElse(null);
  }

  /** Resolves an {@link Anniversary} catalog entry by its identifier. */
  default Anniversary toAnniversary(Long id, @Context CatalogContext catalogs) {
    String msg = "Anniversary not found for id=" + id;
    return Optional.ofNullable(id)
        .map($ -> find(catalogs.anniversaries(), l -> Objects.equals(l.getId(), id), msg))
        .orElse(null);
  }

  /**
   * Maps a distributor request into a {@link FigurineDistributor} entity.
   *
   * <p>This mapping represents distributor-specific commercial data (pricing, preorder,
   * announcement, release).
   *
   * <ul>
   *   <li>{@code id} is ignored and generated by persistence
   *   <li>{@code figurine} is set later during service orchestration
   *   <li>{@code distributor} is resolved using the catalog context
   * </ul>
   *
   * @param distributorReq distributor request payload
   * @param catalogs catalog context used to resolve the distributor
   * @return a new {@link FigurineDistributor} entity
   */
  @Mapping(target = "id", ignore = true) // populated by DB
  @Mapping(target = "figurine", ignore = true) // will be set later in service
  @Mapping(target = "distributor", source = "supplierId")
  @Mapping(target = "announcementDate", source = "announcedAt")
  @Mapping(target = "preorderDate", source = "preorderOpensAt")
  FigurineDistributor toDistributor(
      DistributorReq distributorReq, @Context CatalogContext catalogs);

  /**
   * Resolves a {@link Distributor} catalog entry by its identifier.
   *
   * @param id distributor identifier
   * @param catalogs catalog context
   * @return matching {@link Distributor}, or {@code null} if the id is {@code null}
   * @throws IllegalArgumentException if the distributor does not exist
   */
  default Distributor mapDistributorId(Long id, @Context CatalogContext catalogs) {
    String msg = "Distributor not found for id=" + id;
    return Optional.ofNullable(id)
        .map($ -> find(catalogs.distributors(), l -> Objects.equals(l.getId(), id), msg))
        .orElse(null);
  }

  /* ============================
  Figurine → API
  ============================ */
  /**
   * Maps a {@link Figurine} domain entity to its API response representation.
   *
   * <p>This mapping adapts internal domain naming conventions and boolean flags to a
   * client-friendly response format.
   *
   * <p>Derived fields such as {@code displayableName} are computed using helper methods.
   *
   * @param figurine domain entity
   * @return API-facing {@link FigurineResp}
   */
  @Mapping(target = "name", source = "normalizedName")
  @Mapping(target = "displayableName", expression = "java(createDisplayableName.apply(figurine))")
  @Mapping(target = "lineUp", source = "lineup")
  @Mapping(target = "isMetalBody", source = "metalBody")
  @Mapping(target = "isOriginalColorEdition", source = "oce")
  @Mapping(target = "isRevival", source = "revival")
  @Mapping(target = "isPlainCloth", source = "plainCloth")
  @Mapping(target = "isBattleDamaged", source = "broken")
  @Mapping(target = "isGoldenArmor", source = "golden")
  @Mapping(target = "isGold24kEdition", source = "gold")
  @Mapping(target = "isMangaVersion", source = "manga")
  @Mapping(target = "isMultiPack", source = "set")
  @Mapping(target = "isArticulable", source = "articulable")
  @Mapping(target = "notes", source = "remarks")
  @Mapping(target = "officialImageUrls", source = "officialImages")
  @Mapping(target = "unofficialImageUrls", source = "nonOfficialImages")
  @Mapping(target = "createdAt", source = "creationDate")
  @Mapping(target = "updatedAt", source = "updateDate")
  FigurineResp toFigurineResp(
      Figurine figurine,
      @Context Function<Figurine, String> createDisplayableName,
      @Context Function<FigurineDistributor, Double> calculatePriceWithTax);

  /**
   * Maps a {@link FigurineDistributor} domain entity to its API response representation.
   *
   * <p>The {@code priceWithTax} field is calculated dynamically using the provided pricing
   * function, allowing tax logic to remain outside the mapper.
   *
   * @param figurineDistributor distributor-specific figurine data
   * @param createDisplayableName function used to compute the figurine display name
   * @param calculatePriceWithTax function used to compute the final price including tax
   * @return API-facing {@link FigurineDistributorResp}
   */
  @Mapping(
      target = "priceWithTax",
      expression = "java(calculatePriceWithTax.apply(figurineDistributor))")
  @Mapping(target = "announcedAt", source = "announcementDate")
  @Mapping(target = "preorderOpensAt", source = "preorderDate")
  FigurineDistributorResp toFigurineDistributorResp(
      FigurineDistributor figurineDistributor,
      @Context Function<Figurine, String> createDisplayableName,
      @Context Function<FigurineDistributor, Double> calculatePriceWithTax);

  /**
   * Maps a {@link Distributor} domain entity to its API response representation.
   *
   * <p>The distributor description exposed by the API is derived from the {@code DistributorName}
   * value object rather than a direct field on the entity. This ensures the response reflects the
   * canonical, localized description defined in the catalog.
   *
   * @param distributor the domain distributor entity
   * @return an API-facing {@link DistributorResp}
   */
  @Mapping(target = "description", expression = "java(distributor.getName().getDescription())")
  @Mapping(target = "countryCode", source = "country")
  DistributorResp toDistributorResp(Distributor distributor);

  /**
   * Maps a {@link FigurineEvent} domain entity to its API response representation.
   *
   * <p>Certain contextual fields such as event type, region, and figurine reference are
   * intentionally ignored and populated later during response enrichment.
   *
   * @param figurineEvent domain event entity
   * @param createDisplayableName function used to compute the figurine display name
   * @param calculatePriceWithTax function used for downstream pricing enrichment
   * @return API-facing {@link FigurineEventResp}
   */
  // @Mapping(target = "date", source = "eventDate")
  // @Mapping(target = "figurine", ignore = true) // map this later
  // FigurineEventResp toFigurineEventResp(
  //    FigurineEvent figurineEvent,
  //    @Context Function<Figurine, String> createDisplayableName,
  //    @Context Function<FigurineDistributor, Double> calculatePriceWithTax);

  /**
   * Updates a {@link Figurine} entity using non-null values from another instance.
   *
   * <p>Null fields in {@code source} are ignored.
   *
   * @param target entity to update
   * @param source new values
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(
      target = "distributors",
      ignore = true) // it is OK, distributors can be managed manually.
  @Mapping(
      target = "events",
      ignore = true) // it is OK, events can be managed separately in their own resource.
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "updateDate", expression = "java(java.time.Instant.now())")
  void updateFigurine(@MappingTarget Figurine target, Figurine source);

  /**
   * Updates a {@link FigurineDistributor} entity using values from another instance.
   *
   * <p>This method is intended for partial updates where the existing distributor entry already
   * belongs to a figurine. Identity and relationship fields are preserved.
   *
   * <ul>
   *   <li>{@code id} is ignored and must not be modified
   *   <li>{@code figurine} association is preserved and managed externally
   * </ul>
   *
   * <p>All mappable fields present in {@code source} will overwrite the corresponding values in
   * {@code target}.
   *
   * @param target distributor entity to update
   * @param source new distributor values
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "figurine", ignore = true)
  void updateFigurineDistributor(
      @MappingTarget FigurineDistributor target, FigurineDistributor source);

  /**
   * Parses a raw event string into a {@link FigurineEvent}.
   *
   * @param raw raw string in {@code M/d/yyyy: description} format
   * @return parsed event or {@code null} if blank
   * @throws IllegalArgumentException if a date format is invalid
   */
  private FigurineEvent parseEventString(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }

    String[] parts = raw.split(":", 2);
    String datePart = parts[0].trim();
    String descriptionPart = parts.length > 1 ? parts[1].trim() : "";

    // Validate date
    LocalDate date;
    try {
      date = LocalDate.parse(datePart, EVENT_DATE_FORMATTER);
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid event date format: '" + datePart + "'", ex);
    }

    FigurineEvent event = new FigurineEvent();
    event.setDescription(descriptionPart);
    event.setEventDate(date);
    // FIXME The following properties are hardcoded, fix them
    event.setType(FigurineEventType.ANNOUNCEMENT);
    event.setRegion(CountryCode.JP);
    return event;
  }

  /**
   * Finds a distributor by country code.
   *
   * @param distributors available distributors
   * @param countryCode country code
   * @return matching distributor
   * @throws IllegalArgumentException if none is found
   */
  private Distributor findDistributorByCountry(
      List<Distributor> distributors, CountryCode countryCode) {
    return distributors.stream()
        .filter(d -> d.getCountry() == countryCode)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Distributor not found for code='" + countryCode + "'"));
  }

  /**
   * Generic helper used to resolve catalog entities.
   *
   * @param list catalog list
   * @param predicate matching condition
   * @param errorMessage error message if not found
   * @param <T> catalog type
   * @return matched catalog entry
   */
  private <T extends BaseId> T find(List<T> list, Predicate<T> predicate, String errorMessage) {
    return list.stream()
        .filter(predicate)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(errorMessage));
  }
}
