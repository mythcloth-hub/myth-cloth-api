package com.mesofi.mythclothapi.figurineimages;

import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.DISTRIBUTORS_DEL;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.FIGURINES_DEL;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.GROUPS_DEL;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.LINE_UP_DEL;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.SERIES_DEL;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.createBasicFigurine;
import static com.mesofi.mythclothapi.utils.FigurineCreatorUtils.removeResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.mesofi.mythclothapi.figurineimages.dto.FigurineImageReq;
import com.mesofi.mythclothapi.figurineimages.dto.FigurineImageResp;
import com.mesofi.mythclothapi.it.ControllerBaseIT;
import com.mesofi.mythclothapi.utils.FigurineIdentifiers;

public class FigurineImageControllerIT extends ControllerBaseIT {

  private static final String IMAGES_BY_FIGURINE = "/figurines/{figurineId}/images";

  @Test
  @DisplayName("Test flow to create and process images")
  void fullCrudFigurineImagesFlow() {

    FigurineIdentifiers figIds = createBasicFigurine(rest);

    // CREATE
    List<String> images1 =
        createFigurineImage(
            figIds.id(),
            URI.create("https://imagizer.imageshack.com/img924/4647/beyAuJ.jpg"),
            true);

    List<String> images2 =
        createFigurineImage(
            figIds.id(),
            URI.create("https://imagizer.imageshack.com/img924/4647/Es2hoO.jpg"),
            true);

    List<String> images3 =
        createFigurineImage(
            figIds.id(),
            URI.create("https://imagizer.imageshack.com/img924/4647/GzzH08.jpg"),
            true);

    assertThat(images1).size().isEqualTo(1);
    assertThat(images2).size().isEqualTo(2);
    assertThat(images3).size().isEqualTo(3);

    // READ
    List<String> allImages = readFigurineImages(figIds.id());
    assertThat(allImages).size().isEqualTo(3);
    assertThat(allImages).containsAll(images3);

    // DELETE
    removeResource(
        rest,
        IMAGES_BY_FIGURINE + "?imageUrl={imageUrl}&isOfficialImage={official}",
        figIds.id(),
        "https://imagizer.imageshack.com/img924/4647/beyAuJ.jpg",
        true);
    removeResource(
        rest,
        IMAGES_BY_FIGURINE + "?imageUrl={imageUrl}&isOfficialImage={official}",
        figIds.id(),
        "https://imagizer.imageshack.com/img924/4647/Es2hoO.jpg",
        true);
    removeResource(
        rest,
        IMAGES_BY_FIGURINE + "?imageUrl={imageUrl}&isOfficialImage={official}",
        figIds.id(),
        "https://imagizer.imageshack.com/img924/4647/GzzH08.jpg",
        true);

    removeResource(rest, FIGURINES_DEL, figIds.id());
    removeResource(rest, DISTRIBUTORS_DEL, figIds.distributorId());
    removeResource(rest, GROUPS_DEL, figIds.groupId());
    removeResource(rest, SERIES_DEL, figIds.seriesId());
    removeResource(rest, LINE_UP_DEL, figIds.lineUpId());
  }

  private List<String> readFigurineImages(Long figurineId) {
    ResponseEntity<FigurineImageResp> response =
        rest.get().uri(IMAGES_BY_FIGURINE, figurineId).retrieve().toEntity(FigurineImageResp.class);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().officialImageUrls();
  }

  private List<String> createFigurineImage(Long figurineId, URI uri, boolean officialImage) {
    FigurineImageReq figurineImageReq = createFigurineImageReq(figurineId, uri, officialImage);

    ResponseEntity<FigurineImageResp> response =
        rest.post()
            .uri(IMAGES_BY_FIGURINE, figurineId)
            .body(figurineImageReq)
            .retrieve()
            .toEntity(FigurineImageResp.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isNotNull();

    return response.getBody().officialImageUrls();
  }

  private FigurineImageReq createFigurineImageReq(Long figurineId, URI uri, boolean officialImage) {
    FigurineImageReq figurineImageReq = new FigurineImageReq();
    figurineImageReq.setImageUrl(uri);
    figurineImageReq.setOfficialImage(officialImage);
    figurineImageReq.setFigurineId(figurineId);

    return figurineImageReq;
  }
}
