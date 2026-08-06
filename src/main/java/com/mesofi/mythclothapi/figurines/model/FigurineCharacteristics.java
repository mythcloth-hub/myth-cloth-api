package com.mesofi.mythclothapi.figurines.model;

import com.mesofi.mythclothapi.common.Descriptive;

public record FigurineCharacteristics(String normalizedName, String lineup, String series, String group,
        Boolean metalBody, Boolean oce, Boolean revival, Boolean plainCloth, Boolean broken, Boolean golden,
        Boolean gold, Boolean manga, Boolean set, Boolean articulable) {

    public static FigurineCharacteristics from(Figurine figurine) {
        return new FigurineCharacteristics(figurine.getNormalizedName(), description(figurine.getLineup()),
                description(figurine.getSeries()), description(figurine.getGroup()), figurine.getMetalBody(),
                figurine.getOce(), figurine.getRevival(), figurine.getPlainCloth(), figurine.getBroken(),
                figurine.getGolden(), figurine.getGold(), figurine.getManga(), figurine.getSet(),
                figurine.getArticulable());
    }

    private static String description(Descriptive descriptive) {
        return descriptive == null ? null : descriptive.getDescription();
    }
}
