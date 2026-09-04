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
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import com.mesofi.mythclothapi.anniversaries.AnniversaryMapper;
import com.mesofi.mythclothapi.anniversaries.model.Anniversary;
import com.mesofi.mythclothapi.catalogs.exceptions.CatalogNotFoundException;
import com.mesofi.mythclothapi.catalogs.model.CatalogContext;
import com.mesofi.mythclothapi.catalogs.model.Distribution;
import com.mesofi.mythclothapi.catalogs.model.Group;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.Series;
import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.CountryCode;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurineimports.FigurineImport;
import com.mesofi.mythclothapi.figurineimports.FigurineImportResp;
import com.mesofi.mythclothapi.figurines.dto.DistributorReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineDistributorResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineRecommendationResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineRestockResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineSummaryResp;
import com.mesofi.mythclothapi.figurines.model.Figurine;

/**
 * MapStruct mapper responsible for converting between figurine import models,
 * API request DTOs, domain entities, and API response DTOs.
 *
 * <p>
 * This mapper centralizes transformation rules for figurines, including catalog
 * reference resolution, distributor and pricing normalization, event parsing,
 * and API-specific field naming conversions.
 *
 * <p>
 * {@link CatalogContext} provides the catalog data required to resolve
 * references without performing database access from within the mapper.
 */
@Mapper(componentModel = "spring", uses = AnniversaryMapper.class)
public interface FigurineMapper {

    /** Formatter used to parse event dates in {@code M/d/yyyy} format. */
    DateTimeFormatter EVENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");

    /**
     * Maps a figurine import result to its API response representation.
     *
     * @param importResult
     *            result of the figurine import operation
     * @return API response containing the import result
     */
    @Mapping(target = "imported", source = "totalImported")
    FigurineImportResp toFigurineImportResp(FigurineImport importResult);

    /*
     * ============================ CSV → Figurine ============================
     */

    /**
     * Maps a CSV row into a {@link Figurine} entity.
     *
     * <p>
     * This mapping is used during bulk imports. Catalog references are resolved
     * using textual descriptions rather than identifiers.
     *
     * @param csv
     *            CSV representation of a figurine
     * @param catalogs
     *            catalog context used to resolve reference data
     * @return a new {@link Figurine} entity ready to be persisted
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "legacyName", source = "originalName")
    @Mapping(target = "normalizedName", source = "baseName")
    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "distributors", expression = "java(toDistributors(csv, catalogs.distributors()))")
    @Mapping(target = "distribution", source = "distributionString")
    @Mapping(target = "lineup", source = "lineupString")
    @Mapping(target = "series", source = "seriesString")
    @Mapping(target = "group", source = "groupString")
    @Mapping(target = "anniversary", expression = "java(toAnniversary(csv.getAnniversaryNumberType(), catalogs))")
    @Mapping(target = "currentReleaseStatus", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "collections", ignore = true)
    @Mapping(target = "stores", ignore = true)
    @Mapping(target = "previousRelease", ignore = true)
    @Mapping(target = "subsequentReleases", ignore = true)
    Figurine toFigurine(FigurineCsv csv, @Context CatalogContext catalogs);

    /**
     * Resolves a {@link Distribution} by its description.
     *
     * @param description
     *            distribution description
     * @param catalogs
     *            catalog context containing available distributions
     * @return matching {@link Distribution}, or {@code null} if the description is
     *         blank
     * @throws CatalogNotFoundException
     *             if no distribution with the specified description exists
     */
    default Distribution toDistribution(String description, @Context CatalogContext catalogs) {
        if (description == null || description.isBlank()) {
            return null;
        }

        String msg = "Distribution not found for description=" + description;
        return (Distribution) find(catalogs.distributions(),
                descriptive -> description.equals(descriptive.getDescription()), msg);
    }

    /**
     * Resolves a {@link LineUp} catalog entry using its textual description.
     *
     * <p>
     * This variant is primarily used during CSV imports where catalog references
     * are provided as human-readable descriptions rather than identifiers.
     *
     * @param description
     *            lineup description
     * @param catalogs
     *            catalog context containing available lineups
     * @return matching {@link LineUp}, or {@code null} if the description is blank
     * @throws CatalogNotFoundException
     *             if no lineup with the specified description exists
     */
    default LineUp toLineUp(String description, @Context CatalogContext catalogs) {
        if (description == null || description.isBlank()) {
            return null;
        }

        String msg = "LineUp not found for description=" + description;
        return (LineUp) find(catalogs.lineUps(), descriptive -> description.equals(descriptive.getDescription()), msg);
    }

    /**
     * Resolves a {@link Series} catalog entry using its textual description.
     *
     * @param description
     *            series description
     * @param catalogs
     *            catalog context containing available series
     * @return matching {@link Series}, or {@code null} if the description is blank
     * @throws CatalogNotFoundException
     *             if no series with the specified description exists
     */
    default Series toSeries(String description, @Context CatalogContext catalogs) {
        if (description == null || description.isBlank()) {
            return null;
        }

        String msg = "Series not found for description=" + description;
        return (Series) find(catalogs.series(), descriptive -> description.equals(descriptive.getDescription()), msg);
    }

    /**
     * Resolves a {@link Group} catalog entry using its textual description.
     *
     * @param description
     *            group description
     * @param catalogs
     *            catalog context containing available groups
     * @return matching {@link Group}, or {@code null} if the description is blank
     * @throws CatalogNotFoundException
     *             if no group with the specified description exists
     */
    default Group toGroup(String description, @Context CatalogContext catalogs) {
        if (description == null || description.isBlank()) {
            return null;
        }

        String msg = "Group not found for description=" + description;
        return (Group) find(catalogs.groups(), descriptive -> description.equals(descriptive.getDescription()), msg);
    }

    /**
     * Resolves an {@link Anniversary} catalog entry using a combined anniversary
     * number and type value.
     *
     * <p>
     * When the anniversary type is provided, both the year and type must match.
     * When the type is absent, only the anniversary year is used for the lookup.
     *
     * @param anniversaryNumberType
     *            combined anniversary year/type value parsed from CSV
     * @param catalogs
     *            catalog context containing available anniversaries
     * @return matching {@link Anniversary}, or {@code null} if the input is
     *         {@code null} or no matching anniversary exists
     */
    default Anniversary toAnniversary(AnniversaryNumberType anniversaryNumberType, @Context CatalogContext catalogs) {
        if (anniversaryNumberType == null) {
            return null;
        }

        if (anniversaryNumberType.getAnniversaryType() == null) {
            return catalogs.anniversaries().stream()
                    .filter(ann -> ann.getYear() == anniversaryNumberType.getAnniversaryNumber()).findFirst()
                    .orElse(null);
        }

        return catalogs.anniversaries().stream()
                .filter(ann -> ann.getYear() == anniversaryNumberType.getAnniversaryNumber())
                .filter(ann -> ann.getType() == anniversaryNumberType.getAnniversaryType()).findFirst().orElse(null);
    }

    /**
     * Builds the distributor entries for a figurine using CSV pricing and release
     * information.
     *
     * <p>
     * A JP or Asia distributor is created when Japanese or Hong Kong pricing or
     * release information is available. A Mexico distributor is created when MXN
     * pricing is available.
     *
     * @param csv
     *            CSV figurine data
     * @param distributors
     *            available distributors
     * @return list of distributor entries
     */
    default List<FigurineDistributor> toDistributors(FigurineCsv csv, @Context List<Distributor> distributors) {

        List<FigurineDistributor> distributorList = new ArrayList<>();
        Optional<LocalDateConfirmed> optJPY = Optional.ofNullable(csv.getReleaseJPY());
        Optional<LocalDateConfirmed> optMXN = Optional.ofNullable(csv.getReleaseMXN());

        if (Objects.nonNull(csv.getPriceJPY()) || Objects.nonNull(csv.getAnnouncementJPY())
                || Objects.nonNull(csv.getReleaseJPY())) {
            FigurineDistributor jpOrHk = new FigurineDistributor();
            jpOrHk.setCurrency(csv.isHk() ? CNY : JPY);
            jpOrHk.setPrice(csv.getPriceJPY());
            jpOrHk.setAnnouncementDate(csv.getAnnouncementJPY());
            jpOrHk.setPreorderDate(csv.getPreorderJPY());
            jpOrHk.setReleaseDate(optJPY.map(LocalDateConfirmed::getDate).orElse(null));
            jpOrHk.setReleaseDateConfirmed(optJPY.map(LocalDateConfirmed::isConfirmed).orElse(false));
            jpOrHk.setDistributor(findDistributorByCountry(distributors, csv.isHk() ? CountryCode.CN : CountryCode.JP));
            distributorList.add(jpOrHk);
        }

        if (Objects.nonNull(csv.getPriceMXN())) {
            FigurineDistributor mx = new FigurineDistributor();
            mx.setCurrency(MXN);
            mx.setPrice(csv.getPriceMXN());
            mx.setPreorderDate(csv.getPreorderMXN());
            mx.setReleaseDate(optMXN.map(LocalDateConfirmed::getDate).orElse(null));
            mx.setReleaseDateConfirmed(optMXN.map(LocalDateConfirmed::isConfirmed).orElse(false));
            mx.setDistributor(findDistributorByCountry(distributors, CountryCode.MX));
            distributorList.add(mx);
        }

        return distributorList;
    }

    /**
     * Converts raw event strings into {@link FigurineEvent} entities.
     *
     * <p>
     * Supported input formats include:
     *
     * <ul>
     * <li>{@code M/d/yyyy}</li>
     * <li>{@code M/d/yyyy: description}</li>
     * <li>{@code M/d/yyyy: region: description}</li>
     * </ul>
     *
     * @param eventStrings
     *            raw event definitions
     * @return parsed event list, or an empty list when the input is {@code null} or
     *         empty
     * @throws IllegalArgumentException
     *             if an event contains an invalid date or region
     */
    default List<FigurineEvent> toFigurineEvents(List<String> eventStrings) {
        if (eventStrings == null || eventStrings.isEmpty()) {
            return new ArrayList<>();
        }

        return eventStrings.stream().map(this::parseEventString).filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /*
     * ============================ API → Figurine ============================
     */

    /**
     * Maps an API request into a {@link Figurine} entity.
     *
     * <p>
     * Catalog references are resolved using identifiers. API-specific boolean
     * property names are mapped to their corresponding domain fields.
     *
     * @param req
     *            API request
     * @param catalogs
     *            catalog context used to resolve catalog references
     * @return a new {@link Figurine} entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "legacyName", ignore = true)
    @Mapping(target = "normalizedName", source = "name")
    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "distribution", source = "distributionId")
    @Mapping(target = "lineup", source = "lineUpId")
    @Mapping(target = "series", source = "seriesId")
    @Mapping(target = "group", source = "groupId")
    @Mapping(target = "anniversary", source = "anniversaryId")
    @Mapping(target = "currentReleaseStatus", ignore = true)
    @Mapping(target = "metalBody", source = "isMetalBody")
    @Mapping(target = "oce", source = "isOriginalColorEdition")
    @Mapping(target = "revival", source = "isRevival")
    @Mapping(target = "plainCloth", source = "isPlainCloth")
    @Mapping(target = "broken", source = "isBattleDamaged")
    @Mapping(target = "golden", source = "isGoldenArmor")
    @Mapping(target = "gold", source = "isGold24kEdition")
    @Mapping(target = "manga", source = "isMangaVersion")
    @Mapping(target = "set", source = "isMultiPack")
    @Mapping(target = "articulable", source = "isArticulable")
    @Mapping(target = "remarks", source = "notes")
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "officialImages", source = "officialImageUrls")
    @Mapping(target = "nonOfficialImages", source = "unofficialImageUrls")
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "collections", ignore = true)
    @Mapping(target = "stores", ignore = true)
    @Mapping(target = "previousRelease", ignore = true)
    @Mapping(target = "subsequentReleases", ignore = true)
    Figurine toFigurine(FigurineReq req, @Context CatalogContext catalogs);

    /**
     * Resolves a {@link Distribution} catalog entry by its identifier.
     *
     * @param id
     *            distribution identifier
     * @param catalogs
     *            catalog context containing available distributions
     * @return matching distribution, or {@code null} if {@code id} is {@code null}
     * @throws CatalogNotFoundException
     *             if no distribution with the specified identifier exists
     */
    default Distribution toDistribution(Long id, @Context CatalogContext catalogs) {
        if (Objects.isNull(id)) {
            return null;
        }

        String msg = "Distribution not found for id=" + id;
        return (Distribution) find(catalogs.distributions(), descriptive -> descriptive.getId().equals(id), msg);
    }

    /**
     * Resolves a {@link LineUp} catalog entry by its identifier.
     *
     * @param id
     *            lineup identifier
     * @param catalogs
     *            catalog context containing available lineups
     * @return matching lineup, or {@code null} if {@code id} is {@code null}
     * @throws CatalogNotFoundException
     *             if no lineup with the specified identifier exists
     */
    default LineUp toLineUp(Long id, @Context CatalogContext catalogs) {
        if (Objects.isNull(id)) {
            return null;
        }

        String msg = "LineUp not found for id=" + id;
        return (LineUp) find(catalogs.lineUps(), descriptive -> descriptive.getId().equals(id), msg);
    }

    /**
     * Resolves a {@link Series} catalog entry by its identifier.
     *
     * @param id
     *            series identifier
     * @param catalogs
     *            catalog context containing available series
     * @return matching series, or {@code null} if {@code id} is {@code null}
     * @throws CatalogNotFoundException
     *             if no series with the specified identifier exists
     */
    default Series toSeries(Long id, @Context CatalogContext catalogs) {
        if (Objects.isNull(id)) {
            return null;
        }

        String msg = "Series not found for id=" + id;
        return (Series) find(catalogs.series(), descriptive -> descriptive.getId().equals(id), msg);
    }

    /**
     * Resolves a {@link Group} catalog entry by its identifier.
     *
     * @param id
     *            group identifier
     * @param catalogs
     *            catalog context containing available groups
     * @return matching group, or {@code null} if {@code id} is {@code null}
     * @throws CatalogNotFoundException
     *             if no group with the specified identifier exists
     */
    default Group toGroup(Long id, @Context CatalogContext catalogs) {
        if (Objects.isNull(id)) {
            return null;
        }

        String msg = "Group not found for id=" + id;
        return (Group) find(catalogs.groups(), descriptive -> descriptive.getId().equals(id), msg);
    }

    /**
     * Resolves an {@link Anniversary} catalog entry by its identifier.
     *
     * @param id
     *            anniversary identifier
     * @param catalogs
     *            catalog context containing available anniversaries
     * @return matching anniversary, or {@code null} if {@code id} is {@code null}
     * @throws CatalogNotFoundException
     *             if no anniversary with the specified identifier exists
     */
    default Anniversary toAnniversary(Long id, @Context CatalogContext catalogs) {
        if (Objects.isNull(id)) {
            return null;
        }

        String msg = "Anniversary not found for id=" + id;
        return find(catalogs.anniversaries(), l -> Objects.equals(l.getId(), id), msg);
    }

    /**
     * Maps a distributor request into a {@link FigurineDistributor} entity.
     *
     * <p>
     * The distributor association is resolved using the supplied catalog context.
     * The figurine association is intentionally left unset and is assigned later by
     * the service layer.
     *
     * @param distributorReq
     *            distributor request payload
     * @param catalogs
     *            catalog context used to resolve the distributor
     * @return a new {@link FigurineDistributor} entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "figurine", ignore = true)
    @Mapping(target = "distributor", source = "supplierId")
    @Mapping(target = "announcementDate", source = "announcedAt")
    @Mapping(target = "preorderDate", source = "preorderOpensAt")
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    FigurineDistributor toDistributor(DistributorReq distributorReq, @Context CatalogContext catalogs);

    /**
     * Resolves a {@link Distributor} catalog entry by its identifier.
     *
     * @param id
     *            distributor identifier
     * @param catalogs
     *            catalog context containing available distributors
     * @return matching distributor, or {@code null} if {@code id} is {@code null}
     * @throws CatalogNotFoundException
     *             if no distributor with the specified identifier exists
     */
    default Distributor mapDistributorId(Long id, @Context CatalogContext catalogs) {
        if (Objects.isNull(id)) {
            return null;
        }

        String msg = "Distributor not found for id=" + id;
        return find(catalogs.distributors(), l -> Objects.equals(l.getId(), id), msg);
    }

    /*
     * ============================ Figurine → API ============================
     */

    /**
     * Maps a {@link Figurine} domain entity to its API response representation.
     *
     * <p>
     * Internal domain naming conventions and boolean flags are adapted to the
     * client-facing response format. Restock information is calculated using the
     * supplied function.
     *
     * @param figurine
     *            figurine domain entity
     * @param calculatePriceWithTax
     *            function used to calculate distributor prices including tax
     * @param toFigurineRestockRespList
     *            function used to create the figurine's restock responses
     * @return API-facing {@link FigurineResp}
     */
    @Mapping(target = "name", source = "normalizedName")
    @Mapping(target = "displayableName", source = "displayName")
    @Mapping(target = "releaseStatus", source = "currentReleaseStatus")
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
    @Mapping(target = "restocks", expression = "java(toFigurineRestockRespList.apply(figurine))")
    @Mapping(target = "createdAt", source = "creationDate")
    @Mapping(target = "updatedAt", source = "updateDate")
    FigurineResp toFigurineResp(Figurine figurine, @Context Function<FigurineDistributor, Double> calculatePriceWithTax,
            @Context Function<Figurine, List<FigurineRestockResp>> toFigurineRestockRespList);

    /**
     * Maps a {@link Figurine} domain entity to a condensed API response.
     *
     * <p>
     * The summary representation contains only the fields required for lightweight
     * figurine listings. The first official image is selected when available.
     *
     * @param figurine
     *            figurine domain entity
     * @return API-facing {@link FigurineSummaryResp}
     */
    @Mapping(target = "displayableName", source = "displayName")
    @Mapping(target = "lineUp", source = "lineup")
    @Mapping(target = "officialImageUrl", source = "officialImages", qualifiedByName = "firstImage")
    FigurineSummaryResp toFigurineSummaryResp(Figurine figurine);

    /**
     * Maps a {@link FigurineDistributor} domain entity to its API response
     * representation.
     *
     * <p>
     * The {@code priceWithTax} field is calculated dynamically using the supplied
     * pricing function, keeping tax calculation outside the mapper.
     *
     * @param figurineDistributor
     *            distributor-specific figurine data
     * @param calculatePriceWithTax
     *            function used to calculate the final price including tax
     * @return API-facing {@link FigurineDistributorResp}
     */
    @Mapping(target = "priceWithTax", expression = "java(calculatePriceWithTax.apply(figurineDistributor))")
    @Mapping(target = "announcedAt", source = "announcementDate")
    @Mapping(target = "preorderOpensAt", source = "preorderDate")
    FigurineDistributorResp toFigurineDistributorResp(FigurineDistributor figurineDistributor,
            @Context Function<FigurineDistributor, Double> calculatePriceWithTax);

    /**
     * Maps a {@link Distributor} domain entity to its API response representation.
     *
     * <p>
     * The distributor description exposed by the API is derived from the
     * distributor's name value object.
     *
     * @param distributor
     *            distributor domain entity
     * @return API-facing {@link DistributorResp}
     */
    @Mapping(target = "description", expression = "java(distributor.getName().getDescription())")
    @Mapping(target = "countryCode", source = "country")
    DistributorResp toDistributorResp(Distributor distributor);

    /**
     * Maps a {@link FigurineEvent} domain entity to its API response
     * representation.
     *
     * <p>
     * Event type, region, and figurine references are intentionally left unchanged
     * by this mapping and may be populated separately during response enrichment.
     *
     * @param figurineEvent
     *            figurine event domain entity
     * @param calculatePriceWithTax
     *            function available for downstream pricing enrichment
     * @return API-facing {@link FigurineEventResp}
     */
    @Mapping(target = "date", source = "eventDate")
    @Mapping(target = "dateConfirmed", source = "eventDateConfirmed")
    @Mapping(target = "description", source = "details")
    FigurineEventResp toFigurineEventResp(FigurineEvent figurineEvent,
            @Context Function<FigurineDistributor, Double> calculatePriceWithTax);

    /**
     * Maps a {@link Figurine} domain entity to a recommendation API response.
     *
     * <p>
     * The recommendation response contains only the fields required for
     * recommendation listings. The first official image is selected when available.
     *
     * @param figurine
     *            figurine domain entity
     * @return API-facing {@link FigurineRecommendationResp}
     */
    @Mapping(target = "name", source = "normalizedName")
    @Mapping(target = "imageUrl", expression = "java(getFirstImage(figurine.getOfficialImages()))")
    FigurineRecommendationResp toFigurineRecommendationResp(Figurine figurine);

    /**
     * Returns the first available image URL for a figurine.
     *
     * @param images
     *            figurine image URLs
     * @return first image URL, or {@code null} when no images are present
     */
    default String getFirstImage(List<String> images) {
        return images == null || images.isEmpty() ? null : images.getFirst();
    }

    /**
     * Updates a {@link Figurine} entity using values from another instance.
     *
     * <p>
     * Identity, audit fields, relationships managed independently, and
     * release-status information are preserved. Null source values are handled
     * according to MapStruct's configured mapping strategy.
     *
     * @param target
     *            figurine entity to update
     * @param source
     *            source containing updated figurine values
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "legacyName", ignore = true)
    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "distributors", ignore = true)
    @Mapping(target = "collections", ignore = true)
    @Mapping(target = "stores", ignore = true)
    @Mapping(target = "currentReleaseStatus", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "previousRelease", ignore = true)
    @Mapping(target = "subsequentReleases", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    void updateFigurine(@MappingTarget Figurine target, Figurine source);

    /**
     * Updates a {@link FigurineDistributor} entity using values from another
     * instance.
     *
     * <p>
     * The entity identifier and figurine association are preserved. The
     * distributor-specific values are updated from the source instance.
     *
     * @param target
     *            distributor entity to update
     * @param source
     *            source containing updated distributor values
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "figurine", ignore = true)
    void updateFigurineDistributor(@MappingTarget FigurineDistributor target, FigurineDistributor source);

    /**
     * Parses a raw event string into a {@link FigurineEvent}.
     *
     * <p>
     * Supported formats are:
     *
     * <ul>
     * <li>{@code M/d/yyyy}</li>
     * <li>{@code M/d/yyyy: description}</li>
     * <li>{@code M/d/yyyy: region: description}</li>
     * </ul>
     *
     * <p>
     * When no region is specified, {@link CountryCode#JP} is used as the default.
     * The event type is currently set to {@link FigurineEventType#ANNOUNCEMENT}.
     *
     * @param raw
     *            raw event definition
     * @return parsed event, or {@code null} if the input is blank
     * @throws IllegalArgumentException
     *             if the date or region cannot be parsed
     */
    private FigurineEvent parseEventString(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String[] parts = raw.split(":", 3);

        String datePart;
        String descriptionPart = "";
        String regionPart = "";

        if (parts.length == 1) {
            datePart = parts[0].trim();
        } else if (parts.length == 2) {
            datePart = parts[0].trim();
            descriptionPart = parts[1].trim();
        } else {
            datePart = parts[0].trim();
            regionPart = parts[1].trim();
            descriptionPart = parts[2].trim();
        }

        LocalDate date;
        try {
            date = LocalDate.parse(datePart, EVENT_DATE_FORMATTER);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid event date format: '" + datePart + "'", ex);
        }

        FigurineEvent event = new FigurineEvent();
        event.setDetails(descriptionPart);
        event.setEventDate(date);
        event.setEventDateConfirmed(true);

        CountryCode countryCode = StringUtils.hasLength(regionPart)
                ? CountryCode.valueOf(regionPart.toUpperCase())
                : CountryCode.JP;

        event.setRegion(countryCode);

        // FIXME The following properties are hardcoded, fix them
        event.setType(FigurineEventType.ANNOUNCEMENT);

        return event;
    }

    /**
     * Finds the first distributor associated with the specified country.
     *
     * @param distributors
     *            available distributors
     * @param countryCode
     *            country whose distributor should be located
     * @return the first matching distributor
     * @throws IllegalArgumentException
     *             if no distributor exists for the specified country
     */
    private Distributor findDistributorByCountry(List<Distributor> distributors, CountryCode countryCode) {
        return distributors.stream().filter(d -> d.getCountry() == countryCode).findFirst().orElseThrow(
                () -> new IllegalArgumentException("Distributor not found for code='" + countryCode + "'"));
    }

    /**
     * Returns the first image from a list of image URLs.
     *
     * <p>
     * This method is exposed as a named MapStruct mapping method and is used when
     * creating lightweight figurine responses.
     *
     * @param images
     *            image URLs
     * @return the first image URL, or {@code null} if the list is {@code null} or
     *         empty
     */
    @Named("firstImage")
    default String firstImage(List<String> images) {
        return images == null || images.isEmpty() ? null : images.getFirst();
    }

    /**
     * Finds the first catalog entry matching the specified predicate.
     *
     * <p>
     * This helper centralizes catalog lookup and provides a consistent exception
     * when the requested entry cannot be found.
     *
     * @param list
     *            catalog entries to search
     * @param predicate
     *            condition used to identify the desired entry
     * @param errorMessage
     *            message used when no matching entry is found
     * @param <T>
     *            catalog entity type
     * @return the first matching catalog entry
     * @throws CatalogNotFoundException
     *             if no entry satisfies the predicate
     */
    private <T extends BaseId> T find(List<T> list, Predicate<T> predicate, String errorMessage) {
        return list.stream().filter(predicate).findFirst()
                .orElseThrow(() -> new CatalogNotFoundException(errorMessage));
    }
}
