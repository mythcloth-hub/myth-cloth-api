package com.mesofi.mythclothapi.collectorscollections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineDetailResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionSummaryResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionSummaryStatsResp;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionAlreadyExistsException;
import com.mesofi.mythclothapi.collectorscollections.exceptions.CollectorCollectionNotFoundException;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionFigurineRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionSummaryProjection;
import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.FigurineFilterFactory;
import com.mesofi.mythclothapi.figurines.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;
import com.mesofi.mythclothapi.figurines.repository.projection.FigurineCatalogSummaryProjection;

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

    private final CollectorCollectionFigurineRepository collectorCollectionFigurineRepository;
    private final CollectorCollectionRepository collectorCollectionRepository;
    private final CollectorRepository collectorRepository;
    private final FigurineRepository figurineRepository;
    private final CollectorMapper collectorMapper;

    /**
     * Adds a single figurine to a specific collection.
     *
     * <p>
     * If the figurine already exists in the collection, this method does not create
     * a duplicate relationship.
     *
     * <p>
     * This method is deprecated because it only supports a single figurine and
     * collection assignment. Use
     * {@link #assignFigurinesToCollections(Long, AssignFigurinesReq)} instead,
     * which supports multiple figurines, multiple collections, quantity updates,
     * and collection creation strategies.
     *
     * @param collectionId
     *            identifier of the target collection
     * @param figurineId
     *            identifier of the figurine to assign
     * @deprecated Use
     *             {@link #assignFigurinesToCollections(Long, AssignFigurinesReq)}
     *             instead.
     */
    @Deprecated
    public void addFigurineToCollection(Long collectionId, Long figurineId) {

        var existingFigurine = figurineRepository.findById(figurineId)
                .orElseThrow(() -> new IllegalArgumentException("Figurine with id " + figurineId + " not found"));

        var existingCollection = collectorCollectionRepository.findById(collectionId)
                .orElseGet(() -> createDefaultCollection(collectionId));

        if (collectorCollectionFigurineRepository.findByCollectionAndFigurine(existingCollection, existingFigurine)
                .isPresent()) {
            throw new IllegalArgumentException(
                    "Figurine with id " + figurineId + " already in collection with id " + collectionId);
        }

        collectorCollectionFigurineRepository.save(buildCollectionFigurine(existingCollection, existingFigurine));
    }

    /**
     * Assigns one or more figurines to one or more collector collections.
     *
     * <p>
     * The assignment behavior depends on the requested
     * {@link CollectionAssignmentMode}.
     *
     * <p>
     * This method:
     *
     * <ul>
     * <li>Validates that all requested figurines exist.
     * <li>Retrieves existing collections or creates new collections depending on
     * the assignment mode.
     * <li>Creates missing figurine-collection relationships.
     * <li>Increases the quantity when the figurine already exists in a collection.
     * </ul>
     *
     * @param collectorId
     *            identifier of the collector performing the assignment
     * @param request
     *            assignment request containing figurines, collections, and
     *            assignment mode
     * @throws FigurineNotFoundException
     *             if any requested figurine does not exist
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     * @throws CollectorCollectionNotFoundException
     *             if an existing collection cannot be found
     * @throws CollectorCollectionAlreadyExistsException
     *             if creating a collection with an existing name
     */
    @CacheEvict(value = {COLLECTOR_SUMMARY_CACHE, COLLECTOR_FIGURINE_CACHE,
            COLLECTION_SUMMARY_CACHE}, allEntries = true)
    public void assignFigurinesToCollections(Long collectorId, @Valid AssignFigurinesReq request) {
        log.info("Assigning figurines {} to collections {} with mode {}", request.figurineIds(),
                request.collectionIds(), request.collectionMode());

        List<Figurine> existingFigurines = retrieveExistingFigurines(request.figurineIds());
        List<CollectorCollection> existingCollections = retrieveExistingCollections(request.collectionMode(),
                collectorId, request.collectionIds(), request.collection());

        for (CollectorCollection existingCollection : existingCollections) {
            for (Figurine existingFigurine : existingFigurines) {
                collectorCollectionFigurineRepository.findByCollectionAndFigurine(existingCollection, existingFigurine)
                        .ifPresentOrElse(existing -> {
                            int currentTotal = existing.getQuantity();
                            currentTotal++;
                            existing.setQuantity(currentTotal);
                            collectorCollectionFigurineRepository.save(existing);
                            log.info(
                                    "This figurine is already in the collection [{}] - '{}'. It wont be added again, but increase "
                                            + "the number of figurines, currently there are {} for figurine [{}] - {}",
                                    existingCollection.getId(), existingCollection.getName(), currentTotal,
                                    existing.getFigurine().getId(), existing.getFigurine().getNormalizedName());
                        }, () -> {
                            CollectorCollectionFigurine collectionFigurine = buildCollectionFigurine(existingCollection,
                                    existingFigurine);
                            collectorCollectionFigurineRepository.save(collectionFigurine);
                            log.info("Added figurine [{}] - '{}' to the collection [{}] - '{}'",
                                    collectionFigurine.getFigurine().getId(),
                                    collectionFigurine.getFigurine().getNormalizedName(), existingCollection.getId(),
                                    existingCollection.getName());
                        });
            }
        }
    }

    /**
     * Retrieves catalog and collection summary statistics for one collector
     * collection.
     *
     * <p>
     * The collector must own the collection. The catalog summary is used to build
     * the overall figurine counts, while the collection summary is mapped to the
     * collection-specific ownership statistics.
     * </p>
     *
     * @param collectorId
     *            identifier of the collector
     * @param collectionId
     *            identifier of the collection to summarize
     * @return summary response with catalog and collection totals
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     * @throws CollectorCollectionNotFoundException
     *             if the collection does not exist or is not owned by the collector
     */
    @Transactional(readOnly = true)
    @Cacheable(value = COLLECTOR_SUMMARY_CACHE, key = "T(java.util.Objects).hash(#collectorId, #collectionId)")
    public CollectorCollectionSummaryResp retrieveCollectionSummary(@Positive Long collectorId,
            @Positive Long collectionId) {

        Collector collectorFound = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new CollectorNotFoundException(collectorId));

        ensureCollectionOwnership(collectorFound, collectionId);

        FigurineCatalogSummaryProjection catalogSummary = figurineRepository.getFigurineCatalogSummary();
        CollectorCollectionCatalogSummaryResp summary = new CollectorCollectionCatalogSummaryResp(
                catalogSummary.getTotalFigurines(), catalogSummary.getTotalAnnounced(),
                catalogSummary.getTotalReleased());

        CollectorCollectionSummaryProjection collectionSummary = collectorCollectionRepository
                .getCollectorCollectionSummary(collectionId);

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
    @Cacheable(value = COLLECTOR_FIGURINE_CACHE, key = "T(java.util.Objects).hash(#collectorId, #collectionId, #page, #size)")
    public Page<CollectorCollectionFigurineResp> retrieveCollectionFigurines(@Positive Long collectorId,
            @Positive Long collectionId, @PositiveOrZero int page, @PositiveOrZero int size) {

        Collector collectorFound = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new CollectorNotFoundException(collectorId));

        CollectorCollection collectionFound = collectorCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectorCollectionNotFoundException(collectionId));

        ensureCollectionOwnership(collectorFound, collectionId);

        // build a map to quickly check if a figurine is already in the collection
        Map<Long, CollectorCollectionFigurine> collectionFigurineMap = collectionFound.getFigurines().stream()
                .collect(Collectors.toMap(ccf -> ccf.getFigurine().getId(), Function.identity()));

        FigurineFilter figurineFilter = FigurineFilterFactory.buildReleasedAndAnnounced();
        return figurineRepository.findPaginated(figurineFilter, PageRequest.of(page, size)).map(figurine -> {

            boolean isCollected = collectionFigurineMap.containsKey(figurine.getId());
            int ownedQuantity = isCollected ? collectionFigurineMap.get(figurine.getId()).getQuantity() : 0;

            return collectorMapper.toCollectorCollectionFigurineResp(figurine, figurine.getCurrentReleaseStatus(),
                    isCollected, ownedQuantity);
        });
    }

    /**
     * Retrieves detailed information about a specific figurine within a
     * collector-owned collection context.
     *
     * <p>
     * This operation validates that the collection exists and belongs to the
     * provided collector before retrieving figurine details.
     *
     * @param collectorId
     *            identifier of the collector
     * @param collectionId
     *            identifier of the collection
     * @param figurineId
     *            identifier of the figurine
     * @return detailed figurine information
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     * @throws CollectorCollectionNotFoundException
     *             if the collection does not exist or does not belong to the
     *             collector
     * @throws FigurineNotFoundException
     *             if the figurine does not exist
     */
    @Transactional(readOnly = true)
    public CollectorCollectionFigurineDetailResp retrieveCollectionFigurine(@Positive Long collectorId,
            @Positive Long collectionId, @Positive Long figurineId) {

        var collectorFound = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new CollectorNotFoundException(collectorId));

        collectorCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectorCollectionNotFoundException(collectionId));

        // make sure this collector owns the collection to be retrieved.
        collectorFound.getCollections().stream().filter(c -> c.getId().equals(collectionId)).findFirst()
                .orElseThrow(() -> new CollectorCollectionNotFoundException(collectionId));

        var figurineFound = figurineRepository.findById(figurineId)
                .orElseThrow(() -> new FigurineNotFoundException(figurineId));

        return collectorMapper.toCollectorCollectionFigurineDetailResp(figurineFound);
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
    @CacheEvict(value = {COLLECTOR_SUMMARY_CACHE, COLLECTOR_FIGURINE_CACHE,
            COLLECTION_SUMMARY_CACHE}, allEntries = true)
    public void deleteCollectionFigurine(@Positive Long collectorId, @Positive Long collectionId,
            @Positive Long figurineId) {
        log.info("Deleting figurine [{}] from collection [{}] for collector [{}]", figurineId, collectionId,
                collectorId);

        var collectorFound = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new CollectorNotFoundException(collectorId));

        var collectionFound = collectorCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new CollectorCollectionNotFoundException(collectionId));

        // make sure this collector owns the collection to be deleted.
        collectorFound.getCollections().stream().filter(c -> c.getId().equals(collectionId)).findFirst()
                .orElseThrow(() -> new CollectorCollectionNotFoundException(collectionId));

        var figurineFound = figurineRepository.findById(figurineId)
                .orElseThrow(() -> new FigurineNotFoundException(figurineId));

        collectorCollectionFigurineRepository.findByCollectionAndFigurine(collectionFound, figurineFound)
                .ifPresent(ccf -> {
                    collectorCollectionFigurineRepository.delete(ccf);
                    log.info("Deleted figurine [{}] - '{}' from collection [{}] - '{}'", figurineFound.getId(),
                            figurineFound.getNormalizedName(), collectionFound.getId(), collectionFound.getName());
                });
    }

    /**
     * Retrieves all collections associated with a collector.
     *
     * @param collectorId
     *            identifier of the collector
     * @return list of collector collections
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     */
    @Transactional(readOnly = true)
    @Cacheable(value = COLLECTION_SUMMARY_CACHE, key = "T(java.util.Objects).hash(#collectorId)")
    public List<CollectorCollectionResp> retrieveCollections(final Long collectorId) {
        log.info("Retrieving all collections for collector [{}]", collectorId);

        Collector collectorFound = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new CollectorNotFoundException(collectorId));

        List<CollectorCollection> collectorCollection = collectorFound.getCollections();

        log.info("Found {} collections for collector [{}]", collectorCollection.size(), collectorId);
        return collectorCollection.stream().map(collectorMapper::toCollectorCollectionResp).toList();
    }

    /**
     * Deletes a collector collection and its associated figurine assignments.
     *
     * <p>
     * The collection must belong to the specified collector. Existing
     * collection-figurine rows are removed before deleting the collection entity.
     *
     * @param collectorId
     *            identifier of the collector
     * @param collectionId
     *            identifier of the collection to delete
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     * @throws CollectorCollectionNotFoundException
     *             if the collection does not exist or does not belong to the
     *             collector
     */
    @Transactional
    @CacheEvict(value = {COLLECTOR_SUMMARY_CACHE, COLLECTOR_FIGURINE_CACHE,
            COLLECTION_SUMMARY_CACHE}, allEntries = true)
    public void deleteCollection(Long collectorId, Long collectionId) {
        log.info("Deleting collection [{}] from collector [{}]", collectionId, collectorId);

        Collector collectorFound = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new CollectorNotFoundException(collectorId));

        // make sure this collection owns the collection to be removed.
        collectorFound.getCollections().stream().filter(c -> c.getId().equals(collectionId)).findFirst()
                .orElseThrow(() -> new CollectorCollectionNotFoundException(collectionId));

        // deletes all the figurines associated with the collection.
        int deletedCount = collectorCollectionFigurineRepository.deleteByCollectionIdAndCollectorId(collectionId,
                collectorId);

        // Now it deletes the collection itself
        if (!collectorCollectionRepository.existsById(collectionId)) {
            throw new CollectorCollectionNotFoundException(collectionId);
        }
        collectorCollectionRepository.deleteCollectionById(collectionId);

        log.info("{} items deleted in collection with id {}", deletedCount, collectionId);
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

        Collector collectorFound = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new CollectorNotFoundException(collectorId));

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

        var updated = collectorCollectionRepository.save(existing);

        return new CollectorCollectionResp(updated.getId(), updated.getName(), updated.getImageUrl(),
                updated.getDescription(), 0, List.of());
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

        var collectorFound = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new CollectorNotFoundException(collectorId));

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

        CollectorCollection duplicateCollection = createCollection(collectorId, newName, newImageUrl, newDescription);

        duplicateCollection.setFigurines(owningCollection.getFigurines().stream().map(figurine -> {
            CollectorCollectionFigurine newFigurine = new CollectorCollectionFigurine();
            newFigurine.setCollection(duplicateCollection);
            newFigurine.setFigurine(figurine.getFigurine());
            newFigurine.setQuantity(figurine.getQuantity());
            newFigurine.setCondition(figurine.getCondition());
            return newFigurine;
        }).collect(Collectors.toCollection(ArrayList::new)));

        collectorCollection.add(duplicateCollection); // the new collection is added

        List<CollectorCollection> savedCollections = collectorCollectionRepository.saveAllAndFlush(collectorCollection);

        return savedCollections.stream().filter(sc -> sc.getName().equals(newName)).findFirst()
                .orElseThrow(() -> new CollectorCollectionNotFoundException(0L)).getId();
    }

    /**
     * Resolves all requested figurine identifiers to existing figurine entities.
     *
     * @param figurineIds
     *            figurine identifiers to resolve
     * @return list of existing figurines matching the provided identifiers
     * @throws FigurineNotFoundException
     *             if any identifier does not correspond to an existing figurine
     */
    private List<Figurine> retrieveExistingFigurines(List<Long> figurineIds) {
        // if any of the passed figurineIds, then report it.
        return figurineIds.stream().map(figurineId -> figurineRepository.findById(figurineId)
                .orElseThrow(() -> new FigurineNotFoundException(figurineId))).toList();
    }

    /**
     * Resolves target collections according to the requested assignment mode.
     *
     * @param mode
     *            assignment mode driving how collections are resolved
     * @param collectorId
     *            identifier of the collector
     * @param collectionIds
     *            existing collection identifiers used in {@code EXISTING} mode
     * @param collectionReq
     *            collection payload used in {@code CREATE} mode
     * @return list of resolved collections
     * @throws CollectorNotFoundException
     *             if collector lookup is required and fails
     * @throws CollectorCollectionNotFoundException
     *             if an existing collection id cannot be found
     * @throws CollectorCollectionAlreadyExistsException
     *             if creating a collection conflicts by name
     * @throws IllegalArgumentException
     *             if the mode is unsupported
     */
    private List<CollectorCollection> retrieveExistingCollections(CollectionAssignmentMode mode, Long collectorId,
            List<Long> collectionIds, CollectorCollectionReq collectionReq) {
        List<CollectorCollection> existingCollections = new ArrayList<>();
        if (mode == CollectionAssignmentMode.AUTO) {
            existingCollections.add(createDefaultCollection(collectorId));
        } else if (mode == CollectionAssignmentMode.CREATE) {
            log.info("Creating a new collection for collector [{}] with name '{}'", collectorId, collectionReq.name());

            existingCollections.add(createCollection(collectorId, collectionReq.name(), collectionReq.imageUrl(),
                    collectionReq.description()));
        } else if (mode == CollectionAssignmentMode.EXISTING) {
            log.info("Assigning figurines to existing collections {} for collector [{}]", collectionIds, collectorId);

            existingCollections
                    .addAll(collectionIds.stream()
                            .map(collectionId -> collectorCollectionRepository.findById(collectionId)
                                    .orElseThrow(() -> new CollectorCollectionNotFoundException(collectionId)))
                            .toList());
        } else {
            throw new IllegalArgumentException("Unsupported collection assignment mode: " + mode);
        }
        return existingCollections;
    }

    /**
     * Retrieves or creates the collector default collection.
     *
     * @param collectorId
     *            identifier of the collector
     * @return collector default collection
     */
    private CollectorCollection createDefaultCollection(Long collectorId) {
        return createCollection(collectorId, "My Myth Collection", null, null);
    }

    /**
     * Creates a new collection for the specified collector.
     *
     * <p>
     * The collection name must be unique across collections. If another collection
     * already exists with the same name, creation fails.
     *
     * @param collectorId
     *            identifier of the collector owning the collection
     * @param name
     *            collection name
     * @param description
     *            optional collection description
     * @return newly created collection entity
     * @throws CollectorNotFoundException
     *             if the collector does not exist
     * @throws CollectorCollectionAlreadyExistsException
     *             if a collection with the same name already exists
     */
    private CollectorCollection createCollection(Long collectorId, String name, String imageUrl, String description) {
        Collector collector = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new CollectorNotFoundException(collectorId));

        collectorCollectionRepository.findByCollectorAndName(collector, name).ifPresent(existing -> {
            throw new CollectorCollectionAlreadyExistsException(existing.getName());
        });

        CollectorCollection collectorCollection = new CollectorCollection();
        collectorCollection.setCollector(collector);
        collectorCollection.setName(name);
        collectorCollection.setImageUrl(imageUrl);
        collectorCollection.setDescription(description);

        return collectorCollectionRepository.save(collectorCollection);
    }

    /**
     * Creates a collection-figurine association entity.
     *
     * @param collection
     *            owning collection
     * @param figurine
     *            associated figurine
     * @return new collection-figurine association
     */
    private CollectorCollectionFigurine buildCollectionFigurine(CollectorCollection collection, Figurine figurine) {
        CollectorCollectionFigurine collectorCollectionFigurine = new CollectorCollectionFigurine();
        collectorCollectionFigurine.setCollection(collection);
        collectorCollectionFigurine.setFigurine(figurine);
        return collectorCollectionFigurine;
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
}
