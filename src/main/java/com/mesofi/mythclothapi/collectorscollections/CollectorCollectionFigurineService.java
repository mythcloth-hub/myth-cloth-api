package com.mesofi.mythclothapi.collectorscollections;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectors.mapper.CollectorMapper;
import com.mesofi.mythclothapi.collectorscollections.dto.AssignFigurinesReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectionAssignmentMode;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionCatalogSummaryResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionLatestFavoriteResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionSummaryResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionSummaryStatsResp;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionAlreadyExistsException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionLimitReachedException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionItem;
import com.mesofi.mythclothapi.collectorscollections.model.Condition;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionItemRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.projection.CollectorCollectionCatalogProjection;
import com.mesofi.mythclothapi.collectorscollections.repository.projection.CollectorCollectionSummaryProjection;
import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.FigurineFilterFactory;
import com.mesofi.mythclothapi.figurines.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing the relationship between collectors,
 * collections, and figurines.
 *
 * <p>
 * This service handles figurine assignment, collection summaries, paginated
 * collection figurine listings, figurine detail retrieval, collection updates,
 * collection duplication, and collection deletion.
 *
 * <p>
 * The assignment workflow supports multiple modes through
 * {@link CollectionAssignmentMode}:
 *
 * <ul>
 * <li>{@code AUTO}: Creates or uses the collector's default collection.
 * <li>{@code CREATE}: Creates a new collection using the provided collection
 * information.
 * <li>{@code EXISTING}: Assigns figurines to existing collections.
 * </ul>
 *
 * <p>
 * Before assigning figurines, this service validates that:
 *
 * <ul>
 * <li>The requested figurines exist.
 * <li>The collector exists when creating or retrieving collections.
 * <li>The target collections exist when using existing collections.
 * <li>New collection names do not conflict with existing collections.
 * </ul>
 *
 * <p>
 * This service coordinates multiple repositories to maintain the association
 * between collectors, collections, and figurines.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class CollectorCollectionFigurineService {

    public static final String COLLECTOR_SUMMARY_CACHE = "collector-summary";
    public static final String COLLECTOR_FIGURINE_CACHE = "collector-figurines";
    public static final String COLLECTION_SUMMARY_CACHE = "collection-summary";
    private static final int MAX_COLLECTIONS_PER_COLLECTOR = 6;

    private final CollectorCollectionItemRepository collectorCollectionItemRepository;
    private final CollectorCollectionRepository collectorCollectionRepository;
    private final CollectorRepository collectorRepository;
    private final FigurineRepository figurineRepository;
    private final CollectorMapper collectorMapper;

    @Transactional
    @CacheEvict(value = {COLLECTOR_SUMMARY_CACHE, COLLECTOR_FIGURINE_CACHE,
            COLLECTION_SUMMARY_CACHE}, allEntries = true)
    public void assignFigurinesToCollections(Long collectorId, @Valid AssignFigurinesReq request) {
        log.info("Assigning figurines using mode: {}", request.collectionMode());

        addFigurinesToCollections(collectorId, request);
    }

    private void addFigurinesToCollections(Long collectorId, AssignFigurinesReq request) {
        switch (request.collectionMode()) {
            case CREATE :
                // Handle new collection creation
                log.info("Creating a new collection for collector [{}] with name '{}'", collectorId,
                        request.collection().name());

                var newCollection = createCollection(collectorId, request.figurineIds(), request.collection());

                log.info("New collection [{}] created for collector [{}] with {} figurines", newCollection.getName(),
                        collectorId, newCollection.getItems().size());
                break;
            case EXISTING :
                // Handle existing collection assignment
                log.info("Assigning figurines to existing collections {} for collector [{}]", request.collectionIds(),
                        collectorId);

                updateCollection(collectorId, request.collectionIds(), request.figurineIds());

                log.info("{} Figurines assigned to existing collections {} for collector [{}]",
                        request.figurineIds().size(), request.collectionIds(), collectorId);
                break;
            case AUTO :
                // Handle automatic collection assignment
                log.info("Automatically assigning figurines to the default collection for collector [{}]", collectorId);

                var defaultCollection = createDefaultCollection(collectorId, request.figurineIds());

                log.info("Default collection [{}] for collector [{}] has been created", defaultCollection.getName(),
                        collectorId);
                break;
            default :
                throw new IllegalArgumentException(
                        "Unsupported collection assignment mode: " + request.collectionMode());
        }
    }

    /**
     * Adds a figurine to the collector's favorite collection.
     *
     * <p>
     * If the collector does not have a favorite collection, then a new favorite
     * collection is created.
     *
     * @param collectorId
     *            identifier of the collector
     * @param figurineId
     *            identifier of the figurine to add
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     * @throws CollectorCollectionNotFoundException
     *             if the collector does not have a favorite collection
     * @throws FigurineNotFoundException
     *             if the figurine does not exist
     */
    @Transactional
    @CacheEvict(value = {COLLECTOR_SUMMARY_CACHE, COLLECTOR_FIGURINE_CACHE,
            COLLECTION_SUMMARY_CACHE}, allEntries = true)
    public void addFigurineToFavoriteCollection(@Positive Long collectorId, @Positive Long figurineId) {
        log.info("Adding figurine [{}] to favorite collection for collector [{}]", figurineId, collectorId);

        Collector collectorFound = retrieveCollector(collectorId);
        List<CollectorCollection> collections = collectorFound.getCollections();
        if (collections.isEmpty()) {
            // Create a new favorite collection if none exists
            AssignFigurinesReq request = new AssignFigurinesReq(List.of(figurineId), CollectionAssignmentMode.AUTO,
                    null, null);

            addFigurinesToCollections(collectorId, request);
        } else {
            // add the existing figurine to the favorite collection
            collections.stream().filter(CollectorCollection::isFavorite).findFirst().ifPresent(favCollection -> {
                AssignFigurinesReq request = new AssignFigurinesReq(new ArrayList<>(List.of(figurineId)),
                        CollectionAssignmentMode.EXISTING, List.of(favCollection.getId()), null);

                addFigurinesToCollections(collectorId, request);
            });
        }
    }

    @Transactional(readOnly = true)
    public CollectorCollectionSummaryResp retrieveCollectionSummary(@Positive Long collectorId,
            @Positive Long collectionId, boolean includeRestocks) {

        Collector collectorFound = retrieveCollector(collectorId);

        ensureCollectionOwnership(collectorFound, collectionId);

        CollectorCollectionCatalogProjection catalogSummary = collectorCollectionRepository
                .getCollectorCollectionCatalog(collectionId, includeRestocks);
        CollectorCollectionCatalogSummaryResp summary = new CollectorCollectionCatalogSummaryResp(
                catalogSummary.getTotalFigurines(), catalogSummary.getTotalAnnounced(),
                catalogSummary.getTotalReleased());

        CollectorCollectionSummaryProjection collectionSummary = collectorCollectionRepository
                .getCollectorCollectionSummary(collectionId, includeRestocks);

        CollectorCollectionSummaryStatsResp collection = collectorMapper
                .toCollectorCollectionSummaryResp(collectionSummary, catalogSummary.getTotalReleased());

        return new CollectorCollectionSummaryResp(summary, collection);
    }

    /**
     * Retrieves all figurines available for a collector collection.
     *
     * <p>
     * The response includes figurine information together with collection-specific
     * ownership data, such as whether the figurine exists in the collection and the
     * owned quantity.
     *
     * <p>
     * The collection must belong to the authenticated collector.
     *
     * @param collectorId
     *            identifier of the collector
     * @param collectionId
     *            identifier of the collection
     * @param includeRestocks
     *            whether to include restocked figurines in the listing
     * @param page
     *            page number for pagination (default: 0)
     * @param size
     *            number of figurines per page for pagination (default: 50, max:
     *            1000)
     * @return list of figurines with collection ownership details
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     * @throws CollectorCollectionNotFoundException
     *             if the collection does not exist or does not belong to the
     *             collector
     */
    @Transactional(readOnly = true)
    public Page<CollectorCollectionFigurineResp> retrieveCollectionFigurines(@Positive Long collectorId,
            @Positive Long collectionId, boolean includeRestocks, @PositiveOrZero int page, @PositiveOrZero int size) {

        Collector collectorFound = retrieveCollector(collectorId);
        CollectorCollection collectionFound = retrieveCollectorCollection(collectionId);

        ensureCollectionOwnership(collectorFound, collectionId);

        // build a map to quickly check if a figurine is already in the collection
        Map<Long, CollectorCollectionItem> collectionFigurineMap = collectionFound.getItems().stream()
                .filter(CollectorCollectionItem::isOwned)
                .collect(Collectors.toMap(ccf -> ccf.getFigurine().getId(), Function.identity()));

        FigurineFilter figurineFilter;
        if (includeRestocks) {
            figurineFilter = FigurineFilterFactory.buildReleasedAndAnnounced();
        } else {
            figurineFilter = FigurineFilterFactory.buildReleasedAndAnnounced(false);
        }

        return figurineRepository.findPaginated(figurineFilter, PageRequest.of(page, size), collectionId)
                .map(figurine -> {

                    boolean isCollected = collectionFigurineMap.containsKey(figurine.getId());
                    int ownedQuantity = isCollected ? collectionFigurineMap.get(figurine.getId()).getQuantity() : 0;

                    return collectorMapper.toCollectorCollectionFigurineResp(figurine,
                            figurine.getCurrentReleaseStatus(), isCollected, ownedQuantity);
                });
    }
    /**
     * Retrieves the latest figurines added to the collector's favorite collection.
     *
     * <p>
     * The collector must have a favorite collection. If no favorite collection is
     * found, an empty list is returned.
     *
     * @param collectorId
     *            identifier of the collector
     * @param limit
     *            maximum number of latest figurines to retrieve
     * @return list of latest figurines in the favorite collection
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     */
    @Transactional(readOnly = true)
    public List<CollectorCollectionLatestFavoriteResp> retrieveLatestFavoriteCollectionFigurines(
            @Positive Long collectorId, @Positive int limit) {

        return findLatestFavoriteCollectionFigurines(collectorId, limit).stream()
                .map(collectorMapper::toCollectorCollectionLatestFavoriteResp).toList();
    }

    /**
     * Finds the latest figurines added to the collector's favorite collection.
     *
     * @param collectorId
     *            identifier of the collector
     * @param limit
     *            maximum number of latest figurines to retrieve
     * @return list of latest figurines in the favorite collection
     */
    public List<CollectorCollectionItem> findLatestFavoriteCollectionFigurines(Long collectorId, int limit) {

        Collector collectorFound = retrieveCollector(collectorId);

        Optional<CollectorCollection> favCollection = collectorFound.getCollections().stream()
                .filter(CollectorCollection::isFavorite).findFirst();

        if (favCollection.isEmpty()) {
            // for some reason, the collector does not have a favorite collection. This
            // should not happen, but just in case, return an empty list.
            return List.of();
        }
        CollectorCollection collection = favCollection.get();

        return collectorCollectionItemRepository.findByCollectionAndOwnedTrueOrderByAddedAtDesc(collection,
                PageRequest.of(0, limit));
    }

    /**
     * Deletes a figurine assignment from a collector collection.
     *
     * <p>
     * The collection must belong to the specified collector. If the figurine is not
     * currently assigned to the collection, no deletion is performed.
     *
     * @param collectorId
     *            identifier of the collector
     * @param collectionId
     *            identifier of the collection
     * @param figurineId
     *            identifier of the figurine to unassign
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     * @throws CollectorCollectionNotFoundException
     *             if the collection does not exist or does not belong to the
     *             collector
     * @throws FigurineNotFoundException
     *             if the figurine does not exist
     */
    @Transactional
    public void deleteCollectionFigurine(@Positive Long collectorId, @Positive Long collectionId,
            @Positive Long figurineId) {
        log.info("Deleting figurine [{}] from collection [{}] for collector [{}]", figurineId, collectionId,
                collectorId);

        var collectorFound = retrieveCollector(collectorId);
        var collectionFound = retrieveCollectorCollection(collectionId);

        // make sure this collector owns the collection to be deleted.
        collectorFound.getCollections().stream().filter(c -> c.getId().equals(collectionId)).findFirst()
                .orElseThrow(() -> new CollectorCollectionNotFoundException(collectionId));

        var figurineFound = figurineRepository.findById(figurineId)
                .orElseThrow(() -> new FigurineNotFoundException(figurineId));

        collectorCollectionItemRepository.findByCollectionAndFigurine(collectionFound, figurineFound).ifPresent(ccf -> {
            ccf.setQuantity(0);
            ccf.setAddedAt(null);
            ccf.setOwned(false);
            log.info("Deleted figurine [{}] - '{}' from collection [{}] - '{}'", figurineFound.getId(),
                    figurineFound.getNormalizedName(), collectionFound.getId(), collectionFound.getName());
        });
    }

    @Transactional(readOnly = true)
    // @Cacheable(value = COLLECTION_SUMMARY_CACHE, key =
    // "T(java.util.Objects).hash(#collectorId)")
    public List<CollectorCollectionResp> retrieveCollections(final Long collectorId) {
        log.info("Retrieving all collections for collector [{}]", collectorId);

        Collector collectorFound = retrieveCollector(collectorId);

        List<CollectorCollection> collectorCollection = collectorFound.getCollections();

        log.info("Found {} collections for collector [{}]", collectorCollection.size(), collectorId);
        return collectorCollection.stream().map(collectorMapper::toCollectorCollectionResp).toList();
    }

    /**
     * Updates the metadata of an existing collector collection.
     *
     * <p>
     * The collection ownership is validated before applying updates.
     *
     * @param collectorId
     *            identifier of the collector
     * @param collectionId
     *            identifier of the collection to update
     * @param request
     *            updated collection information
     * @return updated collection response
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     * @throws CollectorCollectionNotFoundException
     *             if the collection does not exist or does not belong to the
     *             collector
     */
    @Transactional
    @CacheEvict(value = {COLLECTION_SUMMARY_CACHE}, allEntries = true)
    public CollectorCollectionResp updateCollection(@Positive Long collectorId, @Positive Long collectionId,
            @NotNull @Valid CollectorCollectionReq request) {
        log.info("Updating collection with id '{}'. New name: '{}'", collectionId, request.name());

        Collector collectorFound = retrieveCollector(collectorId);

        List<CollectorCollection> collectorCollections = collectorFound.getCollections();

        // make sure this collector owns the collection to be updated.
        var existing = collectorCollections.stream().filter(c -> c.getId().equals(collectionId)).findFirst()
                .orElseThrow(() -> new CollectorCollectionNotFoundException(collectionId));

        // is there any other user collection that contains the same collection name? if
        // so, report it
        collectorCollections.stream().filter(cc -> !cc.getId().equals(collectionId))
                .filter(cc -> cc.getName().equals(request.name())).findFirst().ifPresent(cc -> {
                    throw new CollectorCollectionAlreadyExistsException(cc.getName());
                });

        // No need to use MapStruct when properties are too simple. Just update them
        // directly.
        existing.setName(request.name());
        existing.setImageUrl(request.imageUrl());
        existing.setDescription(request.description());

        return new CollectorCollectionResp(existing.getId(), existing.getName(), existing.getImageUrl(),
                existing.getDescription(), existing.isFavorite(), 0, 0, List.of());
    }

    /**
     * Creates a default collection for the specified collector with the given
     * figurine.
     *
     * @param collectorId
     *            identifier of the collector creating the collection
     * @param figurineIds
     *            list of figurine identifiers to be added to the collection
     * @return the newly created {@link CollectorCollection}
     */
    private CollectorCollection createDefaultCollection(Long collectorId, List<Long> figurineIds) {
        return createCollection(collectorId, figurineIds, new CollectorCollectionReq(false, "My Myth Collection", null,
                "This collection was automatically created for you."));
    }

    /**
     * Duplicates an existing collector collection, including its assigned
     * figurines.
     *
     * <p>
     * The source collection must belong to the provided collector. The duplicate
     * uses the original name and description with a {@code " copy"} suffix and
     * carries over figurine quantity and condition values.
     *
     * @param collectorId
     *            identifier of the collector
     * @param collectionId
     *            identifier of the source collection to duplicate
     * @return identifier of the newly created collection
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     * @throws CollectorCollectionNotFoundException
     *             if the source collection does not exist or is not owned by the
     *             collector
     * @throws CollectorCollectionAlreadyExistsException
     *             if the generated duplicate name already exists
     */
    @Transactional
    @CacheEvict(value = {COLLECTION_SUMMARY_CACHE}, allEntries = true)
    public long duplicateCollection(@Positive Long collectorId, @Positive Long collectionId) {
        log.info("Duplicating collection with id '{}' for collector '{}'", collectionId, collectorId);

        var collectorFound = retrieveCollector(collectorId);

        // Finds all collections belonging to the collector.
        List<CollectorCollection> collectorCollection = collectorCollectionRepository.findByCollector(collectorFound);

        CollectorCollection owningCollection = collectorCollection.stream()
                .filter(cc -> cc.getId().equals(collectionId)).findFirst()
                .orElseThrow(() -> new CollectorCollectionNotFoundException(collectionId));

        String newName = owningCollection.getName() + " copy";
        String newImageUrl = owningCollection.getImageUrl() != null ? owningCollection.getImageUrl() : null;
        String newDescription = owningCollection.getDescription() != null
                ? owningCollection.getDescription() + " copy"
                : null;

        List<CollectorCollectionItem> owningCollectionItems = owningCollection.getItems();

        return createCollectionFromExisting(collectorId, owningCollectionItems,
                new CollectorCollectionReq(false, newName, newImageUrl, newDescription)).getId();
    }

    /**
     * Creates a new collection for the specified collector with the given
     * figurines.
     *
     * @param collectorId
     *            identifier of the collector creating the collection
     * @param figurineIds
     *            list of figurine identifiers to be added to the collection
     * @param collection
     *            details of the collection to be created
     * @return the newly created {@link CollectorCollection}
     */
    private CollectorCollection createCollection(Long collectorId, List<Long> figurineIds,
            CollectorCollectionReq collection) {
        // Validate that the collector exists and that the collection name is unique for
        // this collector.
        CollectorCollection newCollection = prepareCollectionForCreation(collectorId, collection);

        var saved = collectorCollectionRepository.save(newCollection);

        List<CollectorCollectionItem> newItems = new ArrayList<>();
        for (Long figurineId : collection.subCollection()
                ? figurineIds
                : figurineRepository.findAllFigurineIdsWithReleasedOrAnnouncedStatus()) {

            boolean owned = !collection.subCollection() && figurineIds.contains(figurineId);
            newItems.add(createCollectorCollectionItem(saved, figurineId, owned));
        }

        // The collection items are saved in bulk to optimize database operations and
        // ensure consistency.
        List<CollectorCollectionItem> savedItems = collectorCollectionItemRepository.saveAll(newItems);

        newCollection.getItems().addAll(savedItems);

        return newCollection;
    }

    /**
     * Creates a new collection for the specified collector by duplicating an
     * existing collection's items.
     *
     * @param collectorId
     *            identifier of the collector creating the collection
     * @param existingItems
     *            list of items from the existing collection to duplicate
     * @param collection
     *            details of the new collection to be created
     * @return the newly created {@link CollectorCollection}
     */
    private CollectorCollection createCollectionFromExisting(Long collectorId,
            List<CollectorCollectionItem> existingItems, CollectorCollectionReq collection) {
        log.info("Creating a new collection for collector [{}] from existing collection with name '{}'", collectorId,
                collection.name());

        CollectorCollection newCollection = prepareCollectionForCreation(collectorId, collection);

        var saved = collectorCollectionRepository.save(newCollection);

        collectorCollectionItemRepository.saveAll(new ArrayList<>(existingItems.stream().map(collectorMapper::copy)
                .peek(item -> item.setCollection(saved)).collect(Collectors.toList())));
        return newCollection;
    }

    /**
     * Prepares a new {@link CollectorCollection} for creation by validating the
     * collector and ensuring that the collection name is unique.
     *
     * @param collectorId
     *            identifier of the collector creating the collection
     * @param collection
     *            details of the collection to be created
     * @return a new {@link CollectorCollection} instance ready for persistence
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     * @throws CollectorCollectionAlreadyExistsException
     *             if a collection with the same name already exists for the
     *             collector
     * @throws CollectorCollectionLimitReachedException
     *             if the collector has reached the maximum number of allowed
     *             collections
     */
    private CollectorCollection prepareCollectionForCreation(Long collectorId, CollectorCollectionReq collection) {
        Collector collector = retrieveCollector(collectorId);

        collectorCollectionRepository.findByCollectorAndName(collector, collection.name()).ifPresent(existing -> {
            throw new CollectorCollectionAlreadyExistsException(existing.getName());
        });

        // We make sure that the collector has not reached the maximum number of
        // collections allowed.
        long existingCollections = collectorCollectionRepository.countByCollector(collector);
        if (existingCollections >= MAX_COLLECTIONS_PER_COLLECTOR) {
            throw new CollectorCollectionLimitReachedException(collectorId, MAX_COLLECTIONS_PER_COLLECTOR);
        }

        boolean isFavorite = existingCollections == 0; // the first collection created is marked as favorite

        return collectorMapper.toCollectorCollection(collection, isFavorite, collector);
    }

    /**
     * Updates existing collections for the specified collector by adding the given
     * figurines.
     *
     * @param collectorId
     *            identifier of the collector whose collections are to be updated
     * @param collectionIds
     *            list of collection identifiers to be updated
     * @param figurineIds
     *            list of figurine identifiers to be added to the collections
     */
    private void updateCollection(Long collectorId, List<Long> collectionIds, List<Long> figurineIds) {
        Collector collector = retrieveCollector(collectorId);

        collectionIds.stream().peek(collectionId -> ensureCollectionOwnership(collector, collectionId))
                .map(this::retrieveCollectorCollection).forEach(collection -> {
                    log.info("Adding {} figurines to collection [{}] for collector [{}]", figurineIds.size(),
                            collection.getName(), collectorId);

                    List<CollectorCollectionItem> itemList = collection.getItems();
                    for (CollectorCollectionItem item : itemList) {
                        if (figurineIds.contains(item.getFigurine().getId())) {
                            // Perform the necessary action for the matching figurine
                            if (item.isOwned()) {
                                item.setQuantity(item.getQuantity() + 1);
                            } else {
                                item.setOwned(true);
                                item.setQuantity(1);
                            }
                            item.setAddedAt(Instant.now());
                            figurineIds.remove(item.getFigurine().getId());
                        }
                    }
                    if (figurineIds.isEmpty()) {
                        log.info("All figurines have been added to collection [{}] for collector [{}]",
                                collection.getName(), collectorId);
                    } else {
                        log.warn("Adding new figurines to collection [{}] for collector [{}]: {}", collection.getName(),
                                collectorId, figurineIds);

                        for (Long figurineId : figurineIds) {
                            collection.getItems().add(createCollectorCollectionItem(collection, figurineId, false));
                        }
                    }
                });
    }

    /**
     * Creates a new {@link CollectorCollectionItem} for the specified collection
     * and figurine.
     *
     * @param collection
     *            the collector's collection to which the item belongs
     * @param figurineId
     *            the figurine to be added to the collection
     * @param owned
     *            whether the collector owns this figurine
     * @return a new {@link CollectorCollectionItem} instance
     */
    private CollectorCollectionItem createCollectorCollectionItem(CollectorCollection collection, Long figurineId,
            boolean owned) {
        Figurine figurine = new Figurine();
        figurine.setId(figurineId);

        CollectorCollectionItem newItem = new CollectorCollectionItem();
        newItem.setCollection(collection);
        newItem.setFigurine(figurine);
        newItem.setQuantity(owned ? 1 : 0);
        newItem.setOwned(owned);
        newItem.setCondition(Condition.SEALED);
        newItem.setAddedAt(owned ? Instant.now() : null);

        return newItem;
    }

    /**
     * Ensures that the specified collection belongs to the given collector.
     *
     * @param collector
     *            the collector who must own the collection
     * @param collectionId
     *            the identifier of the collection to validate
     * @throws CollectorCollectionNotFoundException
     *             if the collection does not exist or is not owned by the collector
     */
    private void ensureCollectionOwnership(Collector collector, Long collectionId) {
        collector.getCollections().stream()
                .filter(collectorCollection -> collectorCollection.getId().equals(collectionId)).findFirst()
                .orElseThrow(() -> new CollectorCollectionNotFoundException(collectionId));
    }

    /**
     * Retrieves a collector by its identifier.
     *
     * @param collectorId
     *            the identifier of the collector to retrieve
     * @return the collector with the specified identifier
     * @throws CollectorNotFoundException
     *             if no collector with the specified identifier exists
     */
    private Collector retrieveCollector(Long collectorId) {
        return collectorRepository.findById(collectorId).orElseThrow(() -> new CollectorNotFoundException(collectorId));
    }

    /**
     * Retrieves a collector collection by its identifier.
     *
     * @param collectionId
     *            the identifier of the collection to retrieve
     * @return the collector collection with the specified identifier
     * @throws CollectorCollectionNotFoundException
     *             if no collection with the specified identifier exists
     */
    private CollectorCollection retrieveCollectorCollection(Long collectionId) {
        return collectorCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectorCollectionNotFoundException(collectionId));
    }
}
