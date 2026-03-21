package com.mesofi.mythclothapi.figurineimages;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.figurineimages.dto.FigurineImageReq;
import com.mesofi.mythclothapi.figurineimages.dto.FigurineImageResp;
import com.mesofi.mythclothapi.figurineimages.exceptions.ImageAlreadyExistsException;
import com.mesofi.mythclothapi.figurineimages.exceptions.ImageNotFoundException;
import com.mesofi.mythclothapi.figurines.FigurineRepository;
import com.mesofi.mythclothapi.figurines.exceptions.FigurineNotFoundException;
import com.mesofi.mythclothapi.figurines.model.Figurine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service layer responsible for managing figurine image associations.
 *
 * <p>This service encapsulates:
 *
 * <ul>
 *   <li>Adding official and non-official image URLs to a figurine
 *   <li>Retrieving image collections for a figurine
 *   <li>Removing image URLs with validation and error handling
 * </ul>
 *
 * <p>Images are stored as normalized URL strings and are treated as sub-resources of the {@link
 * Figurine} aggregate.
 *
 * <p>All write operations are transactional to ensure consistency between figurines and their
 * associated images.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FigurineImageService {

  private final FigurineRepository figurineRepository;

  /**
   * Adds a new image URL to a figurine.
   *
   * <p>This method:
   *
   * <ul>
   *   <li>Validates the target figurine exists
   *   <li>Selects the appropriate image collection (official or non-official)
   *   <li>Normalizes the image URL before storage
   *   <li>Prevents duplicate image entries
   *   <li>Persists the updated figurine state
   * </ul>
   *
   * @param request validated image creation request
   * @return response containing the updated list of image URLs
   * @throws FigurineNotFoundException if the figurine does not exist
   * @throws ImageAlreadyExistsException if the image URL already exists
   */
  @Transactional
  public FigurineImageResp createFigurineImage(@Valid FigurineImageReq request) {

    var existing = findExistingFigurine(request.getFigurineId());

    List<String> existingImages =
        request.isOfficialImage() ? existing.getOfficialImages() : existing.getNonOfficialImages();

    String normalizedUrl = request.getImageUrl().normalize().toString();

    if (existingImages.contains(normalizedUrl)) {
      log.debug("Image {} already exists for figurine {}", normalizedUrl, request.getFigurineId());
      throw new ImageAlreadyExistsException(request.getImageUrl());
    }

    existingImages.add(normalizedUrl);

    figurineRepository.save(existing);

    return new FigurineImageResp(List.copyOf(existingImages));
  }

  /**
   * Retrieves the images associated with a figurine.
   *
   * <p>This method returns either official or non-official images based on the provided flag.
   *
   * <p>The returned collection is an immutable copy to prevent external modification of the
   * internal state.
   *
   * @param figurineId identifier of the target figurine
   * @param isOfficialImage {@code true} to retrieve official images, {@code false} for non-official
   *     images
   * @return response containing the requested image URLs
   * @throws FigurineNotFoundException if the figurine does not exist
   */
  @Transactional(readOnly = true)
  public FigurineImageResp retrieveFigurineImages(
      @Positive Long figurineId, boolean isOfficialImage) {

    var existing = findExistingFigurine(figurineId);

    return new FigurineImageResp(
        isOfficialImage
            ? List.copyOf(existing.getOfficialImages())
            : List.copyOf(existing.getNonOfficialImages()));
  }

  /**
   * Removes an image URL from a figurine.
   *
   * <p>This method:
   *
   * <ul>
   *   <li>Validates the target figurine exists
   *   <li>Selects the appropriate image collection
   *   <li>Normalizes the provided image URL
   *   <li>Removes the image if present
   *   <li>Persists the updated figurine state
   * </ul>
   *
   * @param figurineId identifier of the target figurine
   * @param imageUrl URL of the image to remove
   * @param isOfficialImage {@code true} if the image is official, {@code false} if non-official
   * @throws FigurineNotFoundException if the figurine does not exist
   * @throws ImageNotFoundException if the image URL is not associated with the figurine
   */
  @Transactional
  public void removeFigurineImage(
      @Positive Long figurineId, URI imageUrl, boolean isOfficialImage) {

    var existing = findExistingFigurine(figurineId);

    List<String> images =
        isOfficialImage ? existing.getOfficialImages() : existing.getNonOfficialImages();

    String normalizedUrl = imageUrl.normalize().toString();

    if (images.remove(normalizedUrl)) {
      figurineRepository.save(existing);
    } else {
      log.warn("Image {} not found in figurine {}", normalizedUrl, figurineId);
      throw new ImageNotFoundException(imageUrl);
    }
  }

  /**
   * Retrieves an existing figurine by its identifier.
   *
   * @param figurineId identifier of the figurine
   * @return managed {@link Figurine} entity
   * @throws FigurineNotFoundException if no figurine exists with the given id
   */
  private Figurine findExistingFigurine(Long figurineId) {
    return figurineRepository
        .findById(figurineId)
        .orElseThrow(() -> new FigurineNotFoundException(figurineId));
  }
}
