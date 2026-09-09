package com.mesofi.mythclothapi.collectorscollections;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorNotFoundException;
import com.mesofi.mythclothapi.collectors.mapper.CollectorMapper;
import com.mesofi.mythclothapi.collectorscollections.dto.AssignFigurinesReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectionAssignmentMode;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionAlreadyExistsException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionLimitReachedException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionItem;
import com.mesofi.mythclothapi.collectorscollections.model.Condition;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionItemRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
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
    public static final String COLLECTION_SUMMARY_CACHE = "collection-summary";
    private static final int MAX_COLLECTIONS_PER_COLLECTOR = 6;

    private final CollectorCollectionItemRepository collectorCollectionItemRepository;
    private final CollectorCollectionRepository collectorCollectionRepository;
    private final CollectorRepository collectorRepository;
    private final FigurineRepository figurineRepository;
    private final CollectorMapper collectorMapper;

    @Transactional
    public void assignFigurinesToCollections(Long collectorId, @Valid AssignFigurinesReq request) {

        addFigurinesToCollections(collectorId, request);
    }

    /**
     * Adds figurines to the specified collections based on the assignment request.
     *
     * @param collectorId
     *            identifier of the collector performing the assignment
     * @param request
     *            assignment request containing figurines, collections, and
     *            assignment mode
     */
    private void addFigurinesToCollections(Long collectorId, @Valid AssignFigurinesReq request) {
        List<CollectorCollection> existingCollections = retrieveExistingCollections(collectorId, request);
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

    private List<CollectorCollection> retrieveExistingCollections(Long collectorId, AssignFigurinesReq request) {
        List<CollectorCollection> existingCollections = new ArrayList<>();

        switch (request.collectionMode()) {
            case CREATE :
                // Handle new collection creation
                log.info("Creating a new collection for collector [{}] with name '{}'", collectorId,
                        request.collection().name());

                existingCollections.add(createCollection(collectorId, request.figurineIds(), request.collection()));
                break;
            case EXISTING :
                // Handle existing collection assignment
                log.info("Assigning figurines to existing collections {} for collector [{}]", request.collectionIds(),
                        collectorId);

                for (Long id : request.collectionIds()) {
                    CollectorCollection collection = retrieveCollectorCollection(id);
                }

                // existingCollections.addAll(request.collectionIds().stream().map(this::retrieveCollectorCollection).toList());
                break;
            case AUTO :
                // Handle automatic collection assignment
            default :
                throw new IllegalArgumentException(
                        "Unsupported collection assignment mode: " + request.collectionMode());
        }

        return existingCollections;
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

        CollectorCollection newCollection = collectorMapper.toCollectorCollection(collection, isFavorite, collector);

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
        newCollection.setItems(savedItems);

        return newCollection;
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
        newItem.setQuantity(1); // Default quantity for new items
        newItem.setOwned(owned);
        newItem.setCondition(Condition.SEALED);

        return newItem;
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
