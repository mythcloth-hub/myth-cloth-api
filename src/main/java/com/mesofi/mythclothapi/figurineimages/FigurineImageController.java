package com.mesofi.mythclothapi.figurineimages;

import java.net.URI;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.figurineimages.dto.FigurineImageReq;
import com.mesofi.mythclothapi.figurineimages.dto.FigurineImageResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller responsible for managing figurine image resources.
 *
 * <p>This controller exposes endpoints to:
 *
 * <ul>
 *   <li>Add official or non-official image URLs to a figurine
 *   <li>Retrieve image collections associated with a figurine
 *   <li>Remove image URLs from a figurine
 * </ul>
 *
 * <p>Images are treated as sub-resources of a figurine and are identified by their URL rather than
 * a standalone identifier.
 *
 * <p>All incoming requests are validated before being delegated to {@link FigurineImageService}.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/figurines/{figurineId}/images")
@RequiredArgsConstructor
public class FigurineImageController {

  private final FigurineImageService service;

  /**
   * Adds a new image URL to a figurine.
   *
   * <p>This endpoint:
   *
   * <ul>
   *   <li>Validates the request payload
   *   <li>Associates the image with the specified figurine
   *   <li>Returns the updated image collection
   * </ul>
   *
   * <p>The response is returned with HTTP {@code 201 Created}.
   *
   * @param figurineId identifier of the target figurine
   * @param figurineImageRequest validated image creation request
   * @return response entity containing the updated image collection
   */
  @PostMapping
  public ResponseEntity<FigurineImageResp> createImage(
      @Positive @PathVariable Long figurineId,
      @Valid @RequestBody FigurineImageReq figurineImageRequest) {

    figurineImageRequest.setFigurineId(figurineId);
    FigurineImageResp response = service.createFigurineImage(figurineImageRequest);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Retrieves images associated with a figurine.
   *
   * <p>The type of images returned is controlled by the {@code isOfficialImage} query parameter.
   *
   * @param figurineId identifier of the target figurine
   * @param isOfficialImage {@code true} to retrieve official images, {@code false} for non-official
   *     images
   * @return response containing the requested image URLs
   */
  @GetMapping
  public FigurineImageResp retrieveImages(
      @Positive @PathVariable Long figurineId,
      @RequestParam(name = "isOfficialImage", required = false, defaultValue = "true")
          boolean isOfficialImage) {

    return service.retrieveFigurineImages(figurineId, isOfficialImage);
  }

  /**
   * Removes an image URL from a figurine.
   *
   * <p>The image to remove is identified by its URL and whether it belongs to the official or
   * non-official image collection.
   *
   * <p>If the image does not exist, an appropriate domain exception is thrown.
   *
   * @param figurineId identifier of the target figurine
   * @param imageUrl URL of the image to remove
   * @param isOfficialImage {@code true} if the image is official, {@code false} if non-official
   * @return empty response with HTTP {@code 204 No Content}
   */
  @DeleteMapping
  public ResponseEntity<Void> removeImage(
      @Positive @PathVariable Long figurineId,
      @RequestParam URI imageUrl,
      @RequestParam(name = "isOfficialImage", defaultValue = "true") boolean isOfficialImage) {

    service.removeFigurineImage(figurineId, imageUrl, isOfficialImage);
    return ResponseEntity.noContent().build();
  }
}
