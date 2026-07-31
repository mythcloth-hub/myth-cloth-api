package com.mesofi.mythclothapi.collectors.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mesofi.mythclothapi.catalogs.model.LineUp;
import com.mesofi.mythclothapi.collectorscollections.CollectorCollection;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineDetailResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionFigurineResp;
import com.mesofi.mythclothapi.collectorscollections.dto.CollectorCollectionResp;
import com.mesofi.mythclothapi.collectorscollections.model.CollectorCollectionFigurine;
import com.mesofi.mythclothapi.common.BaseId;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;
import com.mesofi.mythclothapi.distributors.model.Distributor;
import com.mesofi.mythclothapi.figurinedistributions.model.FigurineDistributor;
import com.mesofi.mythclothapi.figurines.dto.FigurineDistributorResp;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;

@Mapper(componentModel = "spring")
public interface CollectorMapper {

    @Mapping(target = "totalFigurines", expression = "java(collectorCollection.getFigurines().size())")
    @Mapping(target = "figurineIds", expression = "java(getFigurineIds(collectorCollection))")
    CollectorCollectionResp toCollectorCollectionResp(CollectorCollection collectorCollection);

    default List<Long> getFigurineIds(CollectorCollection collection) {
        return collection.getFigurines().stream().map(CollectorCollectionFigurine::getFigurine).map(BaseId::getId)
                .toList();
    }

    @Mapping(target = "name", source = "figurine.normalizedName")
    @Mapping(target = "notes", source = "figurine.remarks")
    @Mapping(target = "imageUrl", expression = "java(getFirstImage(figurine.getOfficialImages()))")
    CollectorCollectionFigurineResp toCollectorCollectionFigurineResp(Figurine figurine, ReleaseStatus releaseStatus,
            boolean isCollected, int ownedQuantity, int year);

    default String getFirstImage(List<String> images) {
        return images == null || images.isEmpty() ? null : images.getFirst();
    }

    @Mapping(target = "displayableName", source = "displayName")
    @Mapping(target = "lineUp", source = "lineup")
    @Mapping(target = "lineUpUrl", expression = "java(calculateLineUpUrl(figurine.getLineup()))")
    CollectorCollectionFigurineDetailResp toCollectorCollectionFigurineDetailResp(Figurine figurine);

    @Mapping(target = "description", expression = "java(distributor.getName().getDescription())")
    @Mapping(target = "countryCode", source = "country")
    DistributorResp toDistributorResp(Distributor distributor);

    @Mapping(target = "priceWithTax", ignore = true)
    @Mapping(target = "announcedAt", source = "announcementDate")
    @Mapping(target = "preorderOpensAt", source = "preorderDate")
    FigurineDistributorResp toFigurineDistributorResp(FigurineDistributor figurineDistributor);

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
