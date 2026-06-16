package com.mesofi.mythclothapi.collectorscollections;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.figurines.dto.FigurineResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/collections/{collectionId}/figurines/{figurineId}")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CollectorCollectionFigurineController {

  private final CollectorCollectionFigurineService service;

  @PutMapping
  @PreAuthorize("hasAuthority('collections:figurines:add')")
  public ResponseEntity<FigurineResp> addFigurineToCollection(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long collectionId,
      @PathVariable Long figurineId) {
    service.addFigurineToCollection(collectionId, figurineId);
    return null;
  }
}
