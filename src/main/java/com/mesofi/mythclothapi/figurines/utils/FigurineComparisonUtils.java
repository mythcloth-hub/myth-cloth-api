package com.mesofi.mythclothapi.figurines.utils;

import java.util.Objects;

import com.mesofi.mythclothapi.common.Descriptive;
import com.mesofi.mythclothapi.figurines.model.Figurine;

public final class FigurineComparisonUtils {

    private FigurineComparisonUtils() {
    }

    public static boolean isRestock(Figurine figurine, Figurine other) {
        return figurine != null && other != null && figurine.getAnniversary() == null && other.getAnniversary() == null
                && hasSameCharacteristics(figurine, other);
    }

    public static boolean hasSameCharacteristics(Figurine figurine, Figurine other) {
        if (figurine == null || other == null) {
            return false;
        }

        return Objects.equals(figurine.getNormalizedName(), other.getNormalizedName())
                && sameDescription(figurine.getLineup(), other.getLineup())
                && sameDescription(figurine.getSeries(), other.getSeries())
                && sameDescription(figurine.getGroup(), other.getGroup())
                && Objects.equals(figurine.getMetalBody(), other.getMetalBody())
                && Objects.equals(figurine.getOce(), other.getOce())
                && Objects.equals(figurine.getRevival(), other.getRevival())
                && Objects.equals(figurine.getPlainCloth(), other.getPlainCloth())
                && Objects.equals(figurine.getBroken(), other.getBroken())
                && Objects.equals(figurine.getGolden(), other.getGolden())
                && Objects.equals(figurine.getGold(), other.getGold())
                && Objects.equals(figurine.getManga(), other.getManga())
                && Objects.equals(figurine.getSet(), other.getSet())
                && Objects.equals(figurine.getArticulable(), other.getArticulable());
    }

    private static boolean sameDescription(Descriptive d1, Descriptive d2) {
        return Objects.equals(d1 == null ? null : d1.getDescription(), d2 == null ? null : d2.getDescription());
    }
}
