package com.mesofi.mythclothapi.catalogs;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.catalogs.dto.CatalogReq;
import com.mesofi.mythclothapi.catalogs.dto.CatalogResp;
import com.mesofi.mythclothapi.catalogs.dto.CatalogType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/catalogs")
@RequiredArgsConstructor
public class CatalogController {

  private final CatalogService service;

  @PostMapping("/{catalogType}")
  public ResponseEntity<CatalogResp> createCatalog(
      @NotNull @Valid @PathVariable CatalogType catalogType,
      @NotNull @Valid @RequestBody CatalogReq request) {

    CatalogResp response = service.createCatalog(catalogType.name(), request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest() // e.g. /api/series
            .path("/{id}") // append /{id}
            .buildAndExpand(response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{catalogType}/{id}")
  public CatalogResp retrieveCatalog(@PathVariable CatalogType catalogType, @PathVariable Long id) {
    return service.retrieveCatalog(catalogType.name(), id);
  }

  @PutMapping("/{catalogType}/{id}")
  public ResponseEntity<CatalogResp> updateCatalog(
      @NotNull @Valid @PathVariable CatalogType catalogType,
      @PathVariable Long id,
      @Valid @RequestBody CatalogReq catalogReq) {
    CatalogResp updated = service.updateCatalog(catalogType.name(), id, catalogReq);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{catalogType}/{id}")
  public ResponseEntity<?> removeCatalog(
      @NotNull @Valid @PathVariable CatalogType catalogType, @PathVariable Long id) {
    service.deleteCatalog(catalogType.name(), id);
    return ResponseEntity.ok().build();
  }
}
