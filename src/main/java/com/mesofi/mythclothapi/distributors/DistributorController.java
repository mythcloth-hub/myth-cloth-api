package com.mesofi.mythclothapi.distributors;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.distributors.dto.DistributorReq;
import com.mesofi.mythclothapi.distributors.dto.DistributorResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/distributors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class DistributorController {

  private final DistributorService service;

  @PostMapping
  @PreAuthorize("hasAuthority('distributors:write')")
  public ResponseEntity<DistributorResp> createDistributor(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DistributorReq distributorRequest) {
    log.info(
        "Creating distributor. UserId: {}, User: {}, Request: {}",
        jwt.getSubject(),
        jwt.getClaimAsString("name"),
        distributorRequest);

    DistributorResp response = service.createDistributor(distributorRequest);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest() // e.g. /api/distributors
            .path("/{id}") // append /{id}
            .buildAndExpand(response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('distributors:read')")
  public DistributorResp retrieveDistributor(@PathVariable Long id) {
    return service.retrieveDistributor(id);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('distributors:read')")
  public List<DistributorResp> retrieveDistributors() {
    return service.retrieveDistributors();
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('distributors:update')")
  public ResponseEntity<DistributorResp> updateDistributor(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long id,
      @Valid @RequestBody DistributorReq distributorRequest) {
    log.info(
        "Updating distributor. UserId: {}, User: {}, Request: {}",
        jwt.getSubject(),
        jwt.getClaimAsString("name"),
        distributorRequest);

    DistributorResp updated = service.updateDistributor(id, distributorRequest);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('distributors:delete')")
  public ResponseEntity<?> removeDistributor(
      @AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    log.info(
        "Deleting distributor. UserId: {}, User: {}, id: {}",
        jwt.getSubject(),
        jwt.getClaimAsString("name"),
        id);

    service.removeDistributor(id);
    return ResponseEntity.noContent().build();
  }
}
