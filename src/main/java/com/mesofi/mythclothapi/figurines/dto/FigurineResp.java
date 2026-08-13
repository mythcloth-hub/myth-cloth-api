package com.mesofi.mythclothapi.figurines.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.figurineevents.dto.FigurineEventResp;
import com.mesofi.mythclothapi.figurines.model.ReleaseStatus;

/**
 * Represents the complete API response for a figurine.
 *
 * <p>
 * This response exposes the figurine's identifying information, catalog
 * classifications, release status, distributor and event data, collectible
 * characteristics, images, restock history, and audit timestamps.
 *
 * @param id
 *            unique identifier of the figurine
 * @param name
 *            normalized figurine name
 * @param displayableName
 *            complete name intended for display to users
 * @param distributors
 *            distributor-specific pricing and release information
 * @param tamashiiUrl
 *            URL of the official Tamashii website entry
 * @param releaseStatus
 *            current release status of the figurine
 * @param distribution
 *            distribution catalog entry
 * @param lineUp
 *            lineup catalog entry
 * @param series
 *            series catalog entry
 * @param group
 *            group catalog entry
 * @param anniversary
 *            anniversary associated with the figurine
 * @param isMetalBody
 *            whether the figurine has a metal body
 * @param isOriginalColorEdition
 *            whether the figurine is an Original Color Edition
 * @param isRevival
 *            whether the figurine is a revival release
 * @param isPlainCloth
 *            whether the figurine is a Plain Cloth release
 * @param isBattleDamaged
 *            whether the figurine represents a battle-damaged version
 * @param isGoldenArmor
 *            whether the figurine features golden armor
 * @param isGold24kEdition
 *            whether the figurine is a 24K gold edition
 * @param isMangaVersion
 *            whether the figurine is based on the manga version
 * @param isMultiPack
 *            whether the figurine is part of a multi-pack or set
 * @param isArticulable
 *            whether the figurine is articulable
 * @param notes
 *            additional notes or remarks about the figurine
 * @param officialImageUrls
 *            URLs of official images
 * @param unofficialImageUrls
 *            URLs of non-official images
 * @param events
 *            events associated with the figurine
 * @param restocks
 *            previous or subsequent releases associated with the figurine
 * @param createdAt
 *            timestamp when the figurine was created
 * @param updatedAt
 *            timestamp when the figurine was last updated
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record FigurineResp(long id, String name, String displayableName, List<FigurineDistributorResp> distributors,
        String tamashiiUrl, ReleaseStatus releaseStatus, CatalogResp distribution, CatalogResp lineUp,
        CatalogResp series, CatalogResp group, AnniversaryResp anniversary, Boolean isMetalBody,
        Boolean isOriginalColorEdition, Boolean isRevival, Boolean isPlainCloth, Boolean isBattleDamaged,
        Boolean isGoldenArmor, Boolean isGold24kEdition, Boolean isMangaVersion, Boolean isMultiPack,
        Boolean isArticulable, String notes, List<String> officialImageUrls, List<String> unofficialImageUrls,
        List<FigurineEventResp> events, List<FigurineRestockResp> restocks, Instant createdAt, Instant updatedAt) {
}