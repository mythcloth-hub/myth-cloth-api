package com.mesofi.mythclothapi.collectors.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.collectors.Collector;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionLatestFavoriteResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionReq;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionSummaryStatsResp;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionItem;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionSummaryProjection;

@Mapper(componentModel = "spring")
public interface CollectorMapper {

    /**
     * Maps a collector collection entity to its summary response.
     *
     * <p>
     * The response exposes the collection metadata together with the number of
     * figurines currently associated with the collection and the ids of those
     * figurines.
     * </p>
     *
     * @param collectorCollection
     *            collection entity to map
     * @return collection response populated from the entity
     */
    @Mapping(target = "isFavorite", source = "favorite")
    @Mapping(target = "collectedFigurines", expression = "java(getCollectedFigurinesCount(collectorCollection))")
    @Mapping(target = "totalFigurines", expression = "java(collectorCollection.getItems().size())")
    @Mapping(target = "figurineIds", expression = "java(getFigurineIds(collectorCollection))")
    CollectorCollectionResp toCollectorCollectionResp(CollectorCollection collectorCollection);

    /**
     * Returns the number of figurines in the supplied collection that are owned by
     * the collector.
     *
     * @param collection
     *            collection whose owned figurines should be counted
     * @return number of owned figurines in the collection
     */
    default int getCollectedFigurinesCount(CollectorCollection collection) {
        return (int) collection.getItems().stream().filter(CollectorCollectionItem::isOwned).count();
    }

    /**
     * Returns the ids of the figurines in the supplied collection.
     *
     * @param collection
     *            collection whose figurines should be extracted
     * @return figurine ids in collection order
     */
    default List<Long> getFigurineIds(CollectorCollection collection) {
        return collection.getItems().stream().map(item -> item.getFigurine().getId()).toList();
    }

    /**
     * Maps a collection summary projection to its response DTO.
     *
     * <p>
     * The projection exposes copy counts and unique figurine counts, while the
     * {@code totalReleased} argument is used to calculate the missing released
     * figurines.
     * </p>
     *
     * @param projection
     *            summary projection returned by the repository
     * @param totalReleased
     *            total number of released figurines in the catalog
     * @return summary response populated from the projection
     */
    @Mapping(target = "preorderedCopies", source = "projection.preorderedQuantity")
    @Mapping(target = "ownedCopies", source = "projection.releasedQuantity")
    @Mapping(target = "preorderedFigurines", source = "projection.preorderedFigurines")
    @Mapping(target = "ownedFigurines", source = "projection.releasedFigurines")
    @Mapping(target = "missingReleasedFigurines", expression = "java(totalReleased - projection.getReleasedFigurines())")
    CollectorCollectionSummaryStatsResp toCollectorCollectionSummaryResp(
            CollectorCollectionSummaryProjection projection, int totalReleased);

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
     * Maps a collector collection item entity to its latest favorite response.
     *
     * @param collectorCollectionItem
     *            collector collection figurine entity to map
     * @return latest favorite response populated from the entity
     */
    @Mapping(target = "id", source = "figurine.id")
    @Mapping(target = "name", source = "figurine.normalizedName")
    @Mapping(target = "imageUrl", expression = "java(getFirstImage(collectorCollectionItem.getFigurine().getOfficialImages()))")
    @Mapping(target = "ownedQuantity", source = "quantity")
    CollectorCollectionLatestFavoriteResp toCollectorCollectionLatestFavoriteResp(
            CollectorCollectionItem collectorCollectionItem);
    /**
     * Maps a collector collection request to a new collector collection entity.
     *
     * <p>
     * The entity will have its id, creation date, and update date ignored. The
     * favorite status and collector will be set from the supplied parameters. The
     * items list will be initialized as empty.
     * </p>
     *
     * @param collectionReq
     *            request containing collection metadata
     * @param isFavorite
     *            whether the collection should be marked as favorite
     * @param collector
     *            collector who owns the collection
     * @return new collector collection entity populated from the request and
     *         parameters
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "favorite", source = "isFavorite")
    @Mapping(target = "collector", source = "collector")
    @Mapping(target = "items", ignore = true)
    CollectorCollection toCollectorCollection(CollectorCollectionReq collectionReq, boolean isFavorite,
            Collector collector);
}
