package com.mesofi.mythclothapi.collectors.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineDetailResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionSummaryStatsResp;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionSummaryProjection;
import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.dto.FigurineDistributorResp;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;

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
    @Mapping(target = "totalFigurines", expression = "java(collectorCollection.getFigurines().size())")
    @Mapping(target = "figurineIds", expression = "java(getFigurineIds(collectorCollection))")
    CollectorCollectionResp toCollectorCollectionResp(CollectorCollection collectorCollection);

    /**
     * Returns the ids of the figurines in the supplied collection.
     *
     * @param collection
     *            collection whose figurines should be extracted
     * @return figurine ids in collection order
     */
    default List<Long> getFigurineIds(CollectorCollection collection) {
        return collection.getFigurines().stream().map(CollectorCollectionFigurine::getFigurine).map(BaseId::getId)
                .toList();
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
     * Maps a figurine entity to the collection figurine summary response.
     *
     * @param figurine
     *            figurine entity to map
     * @param releaseStatus
     *            release status to expose in the response
     * @param isCollected
     *            whether the figurine belongs to the collection
     * @param ownedQuantity
     *            number of copies owned in the collection
     * @return figurine summary response populated from the figurine
     */
    @Mapping(target = "name", source = "figurine.normalizedName")
    @Mapping(target = "notes", source = "figurine.remarks")
    @Mapping(target = "imageUrl", expression = "java(getFirstImage(figurine.getOfficialImages()))")
    CollectorCollectionFigurineResp toCollectorCollectionFigurineResp(Figurine figurine, ReleaseStatus releaseStatus,
            boolean isCollected, int ownedQuantity);

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
     * Maps a figurine entity to its detailed collection response.
     *
     * @param figurine
     *            figurine entity to map
     * @return figurine detail response populated from the entity
     */
    @Mapping(target = "displayableName", source = "displayName")
    @Mapping(target = "lineUp", source = "lineup")
    @Mapping(target = "lineUpUrl", expression = "java(calculateLineUpUrl(figurine.getLineup()))")
    CollectorCollectionFigurineDetailResp toCollectorCollectionFigurineDetailResp(Figurine figurine);

    /**
     * Maps a distributor entity to its response DTO.
     *
     * @param distributor
     *            distributor entity to map
     * @return distributor response populated from the entity
     */
    @Mapping(target = "description", expression = "java(distributor.getName().getDescription())")
    @Mapping(target = "countryCode", source = "country")
    DistributorResp toDistributorResp(Distributor distributor);

    /**
     * Maps a figurine distribution entity to its response DTO.
     *
     * @param figurineDistributor
     *            figurine distribution entity to map
     * @return figurine distributor response populated from the entity
     */
    @Mapping(target = "priceWithTax", ignore = true)
    @Mapping(target = "announcedAt", source = "announcementDate")
    @Mapping(target = "preorderOpensAt", source = "preorderDate")
    FigurineDistributorResp toFigurineDistributorResp(FigurineDistributor figurineDistributor);

    /**
     * Resolves the lineup image URL used in figurine detail responses.
     *
     * @param lineup
     *            lineup whose image should be resolved
     * @return lineup image URL, or {@code null} when no match exists
     */
    default String calculateLineUpUrl(LineUp lineup) {
        if (lineup == null) {
            return null;
        }
        if (lineup.getDescription().equals("Myth Cloth EX")) {
            return "https://imagizer.imageshack.com/img922/1037/VGb1UY.png";
        } else if (lineup.getDescription().equals("Myth Cloth")) {
            return "https://imagizer.imageshack.com/img924/6752/iUnW9X.png";
        } else if (lineup.getDescription().contains("Zero")) {
            return "https://imagizer.imageshack.com/img924/3571/4Lb8pL.png";
        }
        // Todo: Add more lineups and their corresponding URLs as needed
        return null;
    }
}
