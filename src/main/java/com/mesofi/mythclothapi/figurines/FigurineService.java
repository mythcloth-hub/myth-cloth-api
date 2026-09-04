package com.mesofi.mythclothapi.figurines;

import static com.mesofi.mythclothapi.collectorscollections.CollectorCollectionFigurineService.COLLECTOR_FIGURINE_CACHE;
import static com.mesofi.mythclothapi.collectorscollections.CollectorCollectionFigurineService.COLLECTOR_SUMMARY_CACHE;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.ANNOUNCEMENT;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.PREORDER_OPEN;
import static com.mesofi.mythclothapi.figurineevents.model.FigurineEventType.RELEASE;
import static com.mesofi.mythclothapi.figurines.FigurineSimilarityUtils.calculateSimilarity;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.ANNOUNCED;
import static com.mesofi.mythclothapi.figurines.model.ReleaseStatus.RELEASED;
import static com.mesofi.mythclothapi.figurines.utils.FigurineComparisonUtils.isRestock;
import static com.mesofi.mythclothapi.stats.StatisticsService.PRICING_SUMMARY_CACHE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.catalogs.CatalogService;
import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.catalogs.model.LineUpType;
import com.mesofi.mythclothapi.catalogs.repository.LineUpRepository;
import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollectionFigurineService;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.figurinedistributions.model.CurrencyCode;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEvent;
import com.mesofi.mythclothapi.figurineevents.model.FigurineEventType;
import com.mesofi.mythclothapi.figurines.dto.FigurineRecommendationResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineRestockResp;
import com.mesofi.mythclothapi.figurines.dto.FigurineSummaryResp;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;
import com.mesofi.mythclothapi.figurines.model.CachedFigurine;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.FigurineCharacteristics;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;
import com.mesofi.mythclothapi.figurines.repository.CollectablePageImpl;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service layer responsible for managing {@link Figurine} lifecycle operations.
 *
 * <p>
 * This service encapsulates:
 *
 * <ul>
 * <li>Importing figurines from a public Google Drive CSV file
 * <li>Creating and updating figurines
 * <li>Resolving catalog references (series, groups, distributors, etc.)
 * <li>Creating default timeline events (announcement, preorder, release)
 * <li>Calculating region-aware prices and taxes
 * </ul>
 *
 * <p>
 * The service acts as the orchestration layer between:
 *
 * <ul>
 * <li>CSV / API input DTOs
 * <li>Domain entities
 * <li>Catalog repositories
 * </ul>
 *
 * <p>
 * All persistence-related operations are transactional to ensure consistency
 * across figurines, distributors, and events.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FigurineService {

    public static final String FIGURINE_CACHE = "figurines";
    public static final String FIGURINE_SUMMARY_CACHE = "figurine-summary";

    private static final String BY_MYTH_CLOTH_EX_KEY = "by-mythcloth-ex";
    private static final String BY_MYTH_CLOTH_KEY = "by-mythcloth";
    private static final String BY_APPENDIX_KEY = "by-appendix";
    private static final String BY_DD_PANORAMATION_KEY = "by-dd-panoramation";
    private static final String BY_FIGUARTS_ZERO_KEY = "by-figuarts-zero";
    private static final String BY_FIGUARTS_KEY = "by-figuarts";
    private static final String BY_SAINT_CLOTH_LEGEND_KEY = "by-saint-cloth-legend";
    private static final String BY_CROWN_KEY = "by-crown";
    private static final String BY_SAINT_CLOTH_SERIES_KEY = "by-saint-cloth-series";

    private final FigurineMapper mapper;

    private final LineUpRepository lineUpRepository;
    private final FigurineRepository repository;
    private final CurrencyRegionResolver currencyRegionResolver;
    private final CollectorRepository collectorRepository;
    private final CollectorCollectionRepository collectorCollectionRepository;
    private final CollectorCollectionFigurineService collectorCollectionFigurineService;
    private final CacheManager cacheManager;
    private final CatalogService catalogService;

    private static final int MAX_FIGURINES_PER_COLLECTOR = 5;
    private final String ANN_MSG = "First announced as a possible future release.";
    private final String PRE_ORDER_MSG = "Pre-orders are officially open.";
    private final String RELEASE_DATE_MSG = "The global release date has been officially announced.";

    private static final Map<LineUpType, FigurineLineUpCacheConf> LINEUP_CONFIG = Map.of(LineUpType.MYTH_CLOTH_EX,
            new FigurineLineUpCacheConf(BY_MYTH_CLOTH_EX_KEY, "Myth Cloth EX"), LineUpType.MYTH_CLOTH,
            new FigurineLineUpCacheConf(BY_MYTH_CLOTH_KEY, "Myth Cloth"), LineUpType.APPENDIX,
            new FigurineLineUpCacheConf(BY_APPENDIX_KEY, "Appendix"), LineUpType.DD_PANORAMATION,
            new FigurineLineUpCacheConf(BY_DD_PANORAMATION_KEY, "DD Panoramation"), LineUpType.FIGUARTS_ZERO,
            new FigurineLineUpCacheConf(BY_FIGUARTS_ZERO_KEY, "Figuarts Zero Metallic Touch"), LineUpType.FIGUARTS,
            new FigurineLineUpCacheConf(BY_FIGUARTS_KEY, "Figuarts"), LineUpType.SAINT_CLOTH_LEGEND,
            new FigurineLineUpCacheConf(BY_SAINT_CLOTH_LEGEND_KEY, "Saint Cloth Legend"), LineUpType.SAINT_CLOTH_CROWN,
            new FigurineLineUpCacheConf(BY_CROWN_KEY, "Saint Cloth Crown"), LineUpType.SAINT_CLOTH_SERIES,
            new FigurineLineUpCacheConf(BY_SAINT_CLOTH_SERIES_KEY, "Saint Cloth Series"));

    // Is the minimum similarity score required to consider a match valid
    private static final double MIN_SIMILARITY_THRESHOLD = 0.7;

    private static final Predicate<Figurine> IS_RELEASED_OR_ANNOUNCED = figurine -> figurine
            .getCurrentReleaseStatus() == RELEASED || figurine.getCurrentReleaseStatus() == ANNOUNCED;

    void assignPreviousRelease(Figurine figurine, List<Figurine> releasedFigurines) {
        Figurine self = null;

        for (Figurine released : releasedFigurines) {
            if (Objects.equals(figurine.getId(), released.getId())) {
                self = released;
                continue;
            }
            if (isRestock(figurine, released)) {
                if (self != null) {
                    self.setPreviousRelease(released);
                    log.info("[{}] - {} is a restock from [{}] - {}", released.getId(), released.getDisplayName(),
                            figurine.getId(), figurine.getDisplayName());
                    return;
                }
            }
        }
    }

    /**
     * Creates a new {@link Figurine} from an API request.
     *
     * <p>
     * This method:
     *
     * <ul>
     * <li>Maps the request into a domain entity
     * <li>Resolves all catalog references
     * <li>Creates default events and timestamps
     * </ul>
     *
     * @param request
     *            validated figurine creation request
     * @return API response DTO for the created figurine
     */
    @Transactional
    @CacheEvict(value = {FIGURINE_CACHE, FIGURINE_SUMMARY_CACHE, COLLECTOR_SUMMARY_CACHE, COLLECTOR_FIGURINE_CACHE,
            PRICING_SUMMARY_CACHE}, allEntries = true)
    public FigurineResp createFigurine(@NotNull @Valid FigurineReq request) {
        log.info("Creating figurine '{}'", request.name());

        Figurine newFigurine = initializeFigurineForCreate(
                mapper.toFigurine(request, catalogService.retrieveCatalogContext()));

        var saved = repository.saveAndFlush(newFigurine);

        linkToPreviousRelease(saved);
        return mapper.toFigurineResp(saved, this::calculatePriceWithTax, this::buildRestockHistory);
    }

    private void linkToPreviousRelease(Figurine persistedFigurine) {
        if (IS_RELEASED_OR_ANNOUNCED.test(persistedFigurine)) {
            assignPreviousRelease(persistedFigurine, repository.findReleasedOrAnnouncedOrderByFirstReleaseDateDesc());
        }
    }

    /**
     * Retrieves an existing {@link Figurine} by its identifier.
     *
     * <p>
     * This method:
     *
     * <ul>
     * <li>Retrieves the figurine by its id
     * <li>Ensures the figurine exists before mapping
     * <li>Maps the entity to an API response DTO
     * </ul>
     *
     * <p>
     * The operation is executed in a read-only transactional context and includes
     * derived fields such as display name and region-aware pricing.
     *
     * @param id
     *            identifier of the figurine to retrieve
     * @return API response DTO representing the requested figurine
     * @throws FigurineNotFoundException
     *             if no figurine exists with the given id
     */
    @Transactional(readOnly = true)
    public FigurineResp readFigurine(@Positive Long id) {
        log.info("Reading figurine with id '{}'", id);

        var existing = repository.findById(id).orElseThrow(() -> new FigurineNotFoundException(id));
        return mapper.toFigurineResp(existing, this::calculatePriceWithTax, this::buildRestockHistory);
    }

    /**
     * Retrieves a paginated list of figurines matching the provided filter
     * criteria.
     *
     * <p>
     * This method:
     *
     * <ul>
     * <li>Applies the specified {@link FigurineFilter} to search for figurines
     * <li>Returns results in a paginated format using the given page and size
     * parameters
     * <li>Maps each {@link Figurine} entity to a {@link FigurineResp} DTO,
     * including display name, price with tax, and release status
     * <li>Stores responses in the {@code figurines} cache using a key derived from
     * the filter, page, and size
     * </ul>
     *
     * @param filter
     *            the filter criteria to apply when searching for figurines
     * @param page
     *            the page number to retrieve (zero-based)
     * @param size
     *            the number of items per page
     * @return a page of {@link FigurineResp} objects matching the filter
     */
    @Transactional(readOnly = true)
    @Timed(value = "figurine.search", description = "Time spent searching figurines")
    @Cacheable(value = FIGURINE_CACHE, key = "T(java.util.Objects).hash(#filter, #page, #size)")
    public CollectablePageImpl<FigurineResp> filterFigurines(@NotNull FigurineFilter filter, @PositiveOrZero int page,
            @Positive int size) {
        log.info("Reading figurines page '{}', size '{}' and filter: {}", page, size, filter);

        CollectablePageImpl<Figurine> figurines = repository.findPaginated(filter, PageRequest.of(page, size));

        List<FigurineResp> list = figurines.getContent().stream().map(
                figurine -> mapper.toFigurineResp(figurine, this::calculatePriceWithTax, this::buildRestockHistory))
                .toList();

        return new CollectablePageImpl<>(list, figurines.getPageable(), figurines.getTotalElements(),
                figurines.getTotalCollectables());
    }

    private List<FigurineRestockResp> buildRestockHistory(Figurine figurine) {

        List<FigurineRestockResp> history = new ArrayList<>();

        Figurine current = figurine;
        while (current.getPreviousRelease() != null) {
            history.add(toRestockResponse(current));
            current = current.getPreviousRelease();
        }

        return history.isEmpty() ? null : history;
    }

    private FigurineRestockResp toRestockResponse(Figurine figurine) {
        Figurine previousRelease = figurine.getPreviousRelease();

        return new FigurineRestockResp(previousRelease.getId(),
                previousRelease.getDistributors().getFirst().getReleaseDate());
    }

    /**
     * Retrieves the identifiers of all figurines contained in a collector's
     * collection.
     *
     * <p>
     * If the supplied collection identifier is {@code null}, an empty list is
     * returned. The collector must exist; otherwise a
     * {@link CollectorNotFoundException} is thrown.
     *
     * @param collectorId
     *            identifier of the collector
     * @param collectionId
     *            identifier of the collection to inspect; may be {@code null}
     * @return a list containing the ids of all figurines in the specified
     *         collection, or an empty list if the collection does not exist or no
     *         collection id was provided
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     */
    public List<Long> retrieveCollectedFigurineIds(long collectorId, Long collectionId) {
        if (collectionId == null) {
            return List.of();
        }

        Collector collectorFound = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new CollectorNotFoundException(collectorId));

        List<CollectorCollection> collectorCollection = collectorCollectionRepository.findByCollector(collectorFound);

        return collectorCollection.stream().filter(cc -> cc.getId().equals(collectionId)).findFirst()
                .map(collection -> collection.getFigurines().stream().map(CollectorCollectionFigurine::getFigurine)
                        .map(BaseId::getId).toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    @Cacheable(FIGURINE_SUMMARY_CACHE)
    public List<FigurineSummaryResp> retrieveFigurineSummaries(@NotNull FigurineFilter filter) {
        log.info("Retrieving figurines summaries '{}'", filter);

        return repository.findAll(filter).stream().filter(figurine -> figurine.getCurrentReleaseStatus() == RELEASED
                || figurine.getCurrentReleaseStatus() == ANNOUNCED).map(mapper::toFigurineSummaryResp).toList();
    }

    /**
     * Retrieves the identifiers of figurines that are eligible for selection based
     * on the provided filter criteria.
     *
     * <p>
     * Only figurines whose computed {@link ReleaseStatus} is either
     * {@link ReleaseStatus#ANNOUNCED} or {@link ReleaseStatus#RELEASED} are
     * included in the result.
     *
     * @param filter
     *            filter criteria used to search for figurines
     * @return a list containing the identifiers of selectable figurines
     */
    public List<Long> retrieveSelectableFigurines(@NotNull FigurineFilter filter) {
        return repository.findAll(filter).stream().filter(figurine -> figurine.getCurrentReleaseStatus() == ANNOUNCED
                || figurine.getCurrentReleaseStatus() == RELEASED).map(BaseId::getId).toList();
    }

    /**
     * Retrieves a list of recommended figurines for the specified collector.
     *
     * <p>
     * If the collector is {@code null}, the method returns the latest released and
     * announced figurines. Otherwise, personalized recommendations are generated
     * based on the collector's preferences and collection history.
     *
     * @param collectorId
     *            identifier of the collector; may be {@code null} for anonymous
     *            users
     * @param limit
     *            maximum number of recommendations to return
     * @return a list of recommended figurine response DTOs
     */
    public List<FigurineRecommendationResp> retrieveRecommendedFigurines(Long collectorId, int limit) {
        log.info("Retrieving recommendations for collector '{}'", collectorId);

        if (collectorId == null) {
            // anonymous user, return the latest released and announced figurines
            FigurineFilter filter = FigurineFilterFactory.buildReleasedAndAnnounced(false);
            return findDefaultRecommendations(filter, limit);
        } else {
            // personalized recommendations for logged-in users
            List<CollectorCollectionFigurine> partialCollection = collectorCollectionFigurineService
                    .findLatestFavoriteCollectionFigurines(collectorId, MAX_FIGURINES_PER_COLLECTOR);
            log.info("Retrieved {} latest figurines for collector '{}'", partialCollection.size(), collectorId);

            if (partialCollection.isEmpty()) {
                FigurineFilter filter = FigurineFilterFactory.buildReleasedAndAnnounced(false);
                return findDefaultRecommendations(filter, limit);
            }

            // For each figurine in the partial collection, we identify the distinct groups
            // and use those groups to retrieve relevant recommendations.
            Set<Long> distinctGroupIds = new HashSet<>();
            Set<Long> figurineIds = new HashSet<>();

            for (CollectorCollectionFigurine collection : partialCollection) {
                Figurine figurine = collection.getFigurine();

                figurineIds.add(figurine.getId());
                distinctGroupIds.add(figurine.getGroup().getId());
            }

            FigurineFilter filter = FigurineFilterFactory
                    .buildReleasedAnnouncedAndGroups(new ArrayList<>(distinctGroupIds));
            return repository.findPaginated(filter, PageRequest.of(0, limit + partialCollection.size())).stream()
                    .filter(figurine -> !figurineIds.contains(figurine.getId()))
                    .map(mapper::toFigurineRecommendationResp).limit(limit).toList();
        }
    }

    /**
     * Retrieves a default set of recommended figurines based on the provided filter
     * criteria.
     *
     * <p>
     * This method is used to provide recommendations for anonymous users or when no
     * personalized recommendations can be generated.
     *
     * @param filter
     *            filter criteria used to search for figurines
     * @param limit
     *            maximum number of recommendations to return
     * @return a list of recommended figurine response DTOs
     */
    private List<FigurineRecommendationResp> findDefaultRecommendations(FigurineFilter filter, int limit) {
        CollectablePageImpl<Figurine> figurines = repository.findPaginated(filter, PageRequest.of(0, limit));
        return figurines.stream().map(mapper::toFigurineRecommendationResp).toList();
    }

    /**
     * Updates an existing {@link Figurine} with new data provided via an API
     * request.
     *
     * <p>
     * This method:
     *
     * <ul>
     * <li>Retrieves the existing figurine by its identifier
     * <li>Maps mutable fields from the request onto the existing entity
     * <li>Resolves and re-links catalog references as needed
     * <li>Persists the updated entity within a transactional boundary
     * </ul>
     *
     * <p>
     * Fields not present in the request are preserved according to the mapper
     * configuration.
     *
     * @param id
     *            identifier of the figurine to update
     * @param request
     *            validated figurine update request
     * @return API response DTO representing the updated figurine
     * @throws FigurineNotFoundException
     *             if no figurine exists with the given id
     */
    @Transactional
    @CacheEvict(value = {FIGURINE_CACHE, FIGURINE_SUMMARY_CACHE, COLLECTOR_SUMMARY_CACHE, COLLECTOR_FIGURINE_CACHE,
            PRICING_SUMMARY_CACHE}, allEntries = true)
    public FigurineResp updateFigurine(@Positive Long id, @NotNull @Valid FigurineReq request) {
        log.info("Updating figurine with id '{}'. New name: '{}'", id, request.name());

        Figurine existing = repository.findById(id).orElseThrow(() -> new FigurineNotFoundException(id));
        Figurine incoming = mapper.toFigurine(request, catalogService.retrieveCatalogContext());
        initializeFigurineForUpdate(existing, incoming);

        var updated = repository.saveAndFlush(existing);

        if (IS_RELEASED_OR_ANNOUNCED.test(updated)) {
            int total = repository.clearPreviousReleases();
            log.info("Cleared previousRelease for {} figurines", total);
            rebuildRestockHistory(repository.findAll());
        }

        return mapper.toFigurineResp(updated, this::calculatePriceWithTax, this::buildRestockHistory);
    }

    /**
     * Recalculates the restocking history for the supplied figurines.
     *
     * <p>
     * The algorithm compares each imported figurine that has been released or
     * announced against the existing released, non-anniversary figurines ordered
     * from newest to oldest. If a matching earlier release is found, the figurine's
     * {@code previousRelease} reference is updated, rebuilding the restocking
     * chain.
     *
     * @param existingFigurines
     *            the figurines to evaluate after the import
     */
    public void rebuildRestockHistory(List<Figurine> existingFigurines) {
        List<Figurine> releasedFigurines = repository.findReleasedOrAnnouncedOrderByFirstReleaseDateDesc();
        log.info("Found {} released figurines", releasedFigurines.size());

        Map<FigurineCharacteristics, List<Figurine>> index = releasedFigurines.stream()
                .collect(Collectors.groupingBy(FigurineCharacteristics::from));

        log.info("Built restock index with {} characteristic groups", index.size());

        existingFigurines.stream().filter(IS_RELEASED_OR_ANNOUNCED).forEach(figurine -> {
            List<Figurine> restockCandidates = index.getOrDefault(FigurineCharacteristics.from(figurine), List.of());

            assignPreviousRelease(figurine, restockCandidates);
        });

        log.info("Rebuilt restock history");
    }

    /**
     * Deletes an existing {@link Figurine} by its identifier.
     *
     * <p>
     * This method:
     *
     * <ul>
     * <li>Retrieves the figurine by its id
     * <li>Ensures the figurine exists before deletion
     * <li>Removes the figurine from persistence
     * </ul>
     *
     * <p>
     * The operation is logged for traceability. Any associated relationships are
     * handled according to the configured JPA cascade rules.
     *
     * @param id
     *            identifier of the figurine to delete
     * @throws FigurineNotFoundException
     *             if no figurine exists with the given id
     */
    @Transactional
    @CacheEvict(value = {FIGURINE_CACHE, FIGURINE_SUMMARY_CACHE, COLLECTOR_SUMMARY_CACHE, COLLECTOR_FIGURINE_CACHE,
            PRICING_SUMMARY_CACHE}, allEntries = true)
    public void deleteFigurine(@Positive Long id) {
        log.info("Deleting figurine with id '{}'", id);
        var existing = repository.findById(id).orElseThrow(() -> new FigurineNotFoundException(id));

        repository.delete(existing);
    }

    /**
     * Synchronizes distributor entries of a figurine using incoming distributor
     * data.
     *
     * <p>
     * This method performs a currency-based merge between existing and incoming
     * {@link FigurineDistributor} entries:
     *
     * <ul>
     * <li>If a distributor with the same {@link CurrencyCode} already exists, its
     * mutable fields are updated
     * <li>If no matching distributor exists, the incoming entry is linked to the
     * figurine and added to the collection
     * </ul>
     *
     * <p>
     * Distributor identity is determined exclusively by currency. This method *
     * does not handle removal of existing distributors.
     *
     * @param existing
     *            the owning figurine
     * @param incomingDistributors
     *            distributor entries provided by the update request
     */
    private void updateDistributors(Figurine existing, List<FigurineDistributor> incomingDistributors) {
        List<FigurineDistributor> existingDistributors = existing.getDistributors();

        if (Objects.isNull(incomingDistributors)) {
            return;
        }

        for (FigurineDistributor incomingFigurineDist : incomingDistributors) {
            CurrencyCode incomingCurrency = incomingFigurineDist.getCurrency();

            existingDistributors.stream().filter(existingFd -> existingFd.getCurrency().equals(incomingCurrency))
                    .findFirst().ifPresentOrElse(
                            existingFd -> mapper.updateFigurineDistributor(existingFd, incomingFigurineDist), () -> {
                                incomingFigurineDist.setFigurine(existing);
                                existingDistributors.add(incomingFigurineDist);
                            });
        }
    }

    /**
     * Finds the figurine whose display name best matches the provided normalized
     * name within the specified line up.
     * <p>
     * The search is performed by comparing the supplied normalized name against the
     * display name of every available figurine in the given line up using
     * {@link FigurineSimilarityUtils#calculateSimilarity(String, String)}. The
     * figurine with the highest similarity score is returned only if its score
     * meets or exceeds {@code MIN_SIMILARITY_THRESHOLD}.
     *
     * @param lineUp
     *            the line-up to search within
     * @param normalizedName
     *            the normalized figurine name obtained from a store listing
     * @return an {@link Optional} containing the best matching figurine if a
     *         suitable match is found; otherwise {@link Optional#empty()}
     */
    @Transactional(readOnly = true)
    public Optional<Figurine> findBestMatchingFigurine(LineUpType lineUp, String normalizedName) {
        List<CachedFigurine> availableFigurines = getAvailableFigurinesByLineUp(lineUp);

        if (availableFigurines.isEmpty()) {
            return Optional.empty();
        }

        double bestSimilarity = MIN_SIMILARITY_THRESHOLD;
        double currentSimilarity = 0;
        Long bestMatchId = null;

        for (CachedFigurine figurine : availableFigurines) {
            currentSimilarity = calculateSimilarity(figurine.displayName(), normalizedName);

            if (currentSimilarity >= bestSimilarity) {
                bestSimilarity = currentSimilarity;
                bestMatchId = figurine.id();
            }
        }

        if (bestMatchId == null) {
            log.info("No suitable match found for '{}', similarity: {} %.", normalizedName,
                    new BigDecimal(currentSimilarity * 100).setScale(2, RoundingMode.UP));
        }
        return Optional.ofNullable(bestMatchId).flatMap(repository::findById);
    }

    /**
     * Retrieves all released and announced figurines for the specified line up.
     * <p>
     * Results are cached to avoid repeatedly querying the database for the same
     * line-up. The returned entities are treated as read-only and are never
     * modified by this service.
     *
     * @param lineUp
     *            the line-up whose figurines should be retrieved
     * @return the available figurines for the requested line-up, or an empty list
     *         if the line-up is not configured or cannot be found
     */
    private List<CachedFigurine> getAvailableFigurinesByLineUp(LineUpType lineUp) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(FIGURINE_CACHE), "figurines cache not configured");

        FigurineLineUpCacheConf config = LINEUP_CONFIG.get(lineUp);
        if (config == null) {
            return List.of();
        }

        @SuppressWarnings("unchecked")
        List<CachedFigurine> figurines = cache.get(config.cacheKey(), List.class);

        if (figurines == null) {
            Optional<LineUp> lineUpEntity = lineUpRepository.findByDescription(config.lineUpDescription());

            if (lineUpEntity.isEmpty()) {
                log.warn("No line up found for '{}'.", config.lineUpDescription());
                return List.of();
            }

            figurines = repository.findAllByLineup(lineUpEntity.get()).stream()
                    .filter(figurine -> figurine.getCurrentReleaseStatus() == RELEASED
                            || figurine.getCurrentReleaseStatus() == ANNOUNCED)
                    .map(figurine -> new CachedFigurine(figurine.getId(), figurine.getDisplayName())).toList();

            cache.put(config.cacheKey(), figurines);
        }

        return figurines;
    }

    /**
     * Calculates the final price including regional taxes based on currency.
     *
     * <p>
     * If price or distributor information is missing, {@code null} is returned.
     *
     * @param figurineDistributor
     *            distributor pricing information
     * @return price including applicable tax, or {@code null}
     */
    public Double calculatePriceWithTax(FigurineDistributor figurineDistributor) {
        if (figurineDistributor == null || figurineDistributor.getPrice() == null
                || figurineDistributor.getPrice() <= 0) {
            return null;
        }

        return switch (figurineDistributor.getCurrency()) {
            case JPY ->
                calculateJapanesePriceWithTax(figurineDistributor.getPrice(), figurineDistributor.getReleaseDate());
            case MXN -> figurineDistributor.getPrice() * 1.16; // example IVA
            case USD -> figurineDistributor.getPrice(); // no VAT by default
            default -> figurineDistributor.getPrice();
        };
    }

    /**
     * Calculates Japanese consumption tax based on historical tax rates.
     *
     * @param price
     *            base price
     * @param releaseDate
     *            official release date
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
     * <p>
     * This includes:
     *
     * <ul>
     * <li>Creating default events
     * <li>Linking bidirectional relationships
     * <li>Initializing audit timestamps
     * </ul>
     *
     * @param incoming
     *            figurine to prepare
     */
    public Figurine initializeFigurineForCreate(Figurine incoming) {
        createDefaultEvents(incoming);
        linkReferences(incoming);

        return incoming;
    }

    public Figurine initializeFigurineForUpdate(Figurine existing, Figurine incoming) {
        // Ask MapStruct to update fields
        mapper.updateFigurine(existing, incoming);

        // update the distributors' info.
        updateDistributors(existing, incoming.getDistributors());

        // update the events in case it was updated.
        existing.getDistributors().stream().findFirst().ifPresent(fd -> {
            existing.getEvents().stream().sorted(Comparator.comparing(FigurineEvent::getEventDate))
                    .filter(e -> e.getType() == ANNOUNCEMENT).findFirst().ifPresentOrElse(e -> {
                        if (Objects.nonNull(fd.getAnnouncementDate())) {
                            e.setEventDate(fd.getAnnouncementDate());
                        }
                    }, () -> {
                        if (Objects.nonNull(fd.getAnnouncementDate())) {
                            addDefaultEvent(ANN_MSG, fd.getAnnouncementDate(), true, ANNOUNCEMENT, existing);
                        }
                    });

            existing.getEvents().stream().filter(e -> e.getType() == PREORDER_OPEN).findFirst().ifPresentOrElse(e -> {
                if (Objects.nonNull(fd.getPreorderDate())) {
                    e.setEventDate(fd.getPreorderDate());
                }
            }, () -> {
                if (Objects.nonNull(fd.getPreorderDate())) {
                    addDefaultEvent(PRE_ORDER_MSG, fd.getPreorderDate(), true, PREORDER_OPEN, existing);
                }
            });

            existing.getEvents().stream().filter(e -> e.getType() == RELEASE).findFirst().ifPresentOrElse(e -> {
                if (Objects.nonNull(fd.getReleaseDate())) {
                    e.setEventDate(fd.getReleaseDate());
                }
                e.setEventDateConfirmed(fd.isReleaseDateConfirmed());
            }, () -> {
                if (Objects.nonNull(fd.getReleaseDate())) {
                    addDefaultEvent(RELEASE_DATE_MSG, fd.getReleaseDate(), fd.isReleaseDateConfirmed(), RELEASE,
                            existing);
                }
            });

            existing.getEvents().forEach(e -> e.setFigurine(existing));
        });

        return existing;
    }

    /**
     * Creates default timeline events (announcement, preorder, release) based on
     * distributor-provided dates.
     *
     * @param figurine
     *            target figurine
     */
    private void createDefaultEvents(Figurine figurine) {
        // creates the default events ...
        if (Objects.isNull(figurine.getDistributors()) || figurine.getDistributors().isEmpty()) {
            log.warn("Figurine '{}' has no distributors, skipping default event creation", figurine.getLegacyName());
            return;
        }

        FigurineDistributor figurineDistributor = figurine.getDistributors().getFirst();

        Optional.ofNullable(figurineDistributor.getAnnouncementDate()).ifPresent(
                announcementDate -> addDefaultEvent(ANN_MSG, announcementDate, true, ANNOUNCEMENT, figurine));
        Optional.ofNullable(figurineDistributor.getPreorderDate())
                .ifPresent(preorderDate -> addDefaultEvent(PRE_ORDER_MSG, preorderDate, true, PREORDER_OPEN, figurine));
        Optional.ofNullable(figurineDistributor.getReleaseDate())
                .ifPresent(releaseDate -> addDefaultEvent(RELEASE_DATE_MSG, releaseDate,
                        figurineDistributor.isReleaseDateConfirmed(), RELEASE, figurine));
    }

    /**
     * Adds a default {@link FigurineEvent} to a figurine.
     *
     * <p>
     * The event region is resolved from the distributor currency.
     *
     * @param description
     *            event description
     * @param date
     *            event date
     * @param dateConfirmed
     *            whether the event date is confirmed or tentative
     * @param type
     *            event type
     * @param figurine
     *            target figurine
     */
    private void addDefaultEvent(String description, LocalDate date, boolean dateConfirmed, FigurineEventType type,
            Figurine figurine) {

        FigurineEvent event = new FigurineEvent();
        event.setDetails(description);
        event.setEventDate(date);
        event.setEventDateConfirmed(dateConfirmed);
        event.setType(type);
        FigurineDistributor figurineDistributor = figurine.getDistributors().stream().findFirst().orElseThrow();
        CurrencyCode currencyCode = figurineDistributor.getCurrency();

        event.setRegion(currencyRegionResolver.resolveCountry(currencyCode));

        figurine.getEvents().add(event);
    }

    /**
     * Ensures all bidirectional relationships are properly linked before
     * persistence.
     *
     * @param figurine
     *            target figurine
     */
    private void linkReferences(Figurine figurine) {
        if (Objects.nonNull(figurine.getDistributors())) {
            figurine.getDistributors().forEach(d -> d.setFigurine(figurine));
        }
        figurine.getEvents().forEach(e -> e.setFigurine(figurine));
    }

}
