package com.mesofi.mythclothapi.figurines.repository.projection;

public record FigurineSearchProjection(Long id, String normalizedName, String displayName, String currentReleaseStatus,
        String lineupDescription, String groupDescription, String anniversaryDescription, boolean isMetalBody,
        boolean isOce, boolean isRevival, boolean isGold, String imageUrl) {
}
