package com.mesofi.mythclothapi.figurines.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Represents a request to create or update a figurine.
 *
 * <p>
 * The request contains the figurine's name, catalog associations, distributor
 * information, collectible characteristics, notes, and image URLs. Bean
 * Validation annotations enforce the required fields and basic value
 * constraints.
 *
 * @param name
 *            figurine name; must not be blank and must not exceed 100
 *            characters
 * @param distributors
 *            distributor-specific pricing and release information
 * @param tamashiiUrl
 *            URL of the official Tamashii entry; must not exceed 50 characters
 * @param distributionId
 *            identifier of the distribution catalog entry
 * @param lineUpId
 *            identifier of the lineup catalog entry; must be positive
 * @param seriesId
 *            identifier of the series catalog entry; must be positive
 * @param groupId
 *            identifier of the group catalog entry; must be positive when
 *            provided
 * @param anniversaryId
 *            identifier of the associated anniversary; must be positive when
 *            provided
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
 *            URLs of official images associated with the figurine
 * @param unofficialImageUrls
 *            URLs of non-official images associated with the figurine
 */
public record FigurineReq(@NotBlank @Size(max = 100, message = "Name must not exceed 100 characters") String name,
        @Valid List<DistributorReq> distributors,
        @Size(max = 50, message = "Tamashii URL must not exceed 50 characters") String tamashiiUrl, Long distributionId,
        @NotNull @Positive Long lineUpId, @NotNull @Positive Long seriesId, @Positive Long groupId,
        @Positive Long anniversaryId, Boolean isMetalBody, Boolean isOriginalColorEdition, Boolean isRevival,
        Boolean isPlainCloth, Boolean isBattleDamaged, Boolean isGoldenArmor, Boolean isGold24kEdition,
        Boolean isMangaVersion, Boolean isMultiPack, Boolean isArticulable, String notes,
        List<String> officialImageUrls, List<String> unofficialImageUrls) {
}
