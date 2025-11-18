package com.mesofi.mythclothapi.distributors;

import java.net.URI;
import java.util.List;

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

import com.mesofi.mythclothapi.distributors.model.DistributorRequest;
import com.mesofi.mythclothapi.distributors.model.DistributorResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/distributors")
@RequiredArgsConstructor
public class DistributorController {

  private final DistributorService service;

  @PostMapping
  public ResponseEntity<DistributorResponse> createDistributor(
      @Valid @RequestBody DistributorRequest distributorRequest) {
    DistributorResponse response = service.createDistributor(distributorRequest);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest() // e.g. /api/distributors
            .path("/{id}") // append /{id}
            .buildAndExpand(response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  public DistributorResponse retrieveDistributor(@PathVariable Long id) {
    return service.retrieveDistributor(id);
  }

  @GetMapping
  public List<DistributorResponse> retrieveDistributors() {
    return service.retrieveDistributors();
  }

  @PutMapping("/{id}")
  public ResponseEntity<DistributorResponse> updateDistributor(
      @PathVariable Long id, @Valid @RequestBody DistributorRequest distributorRequest) {
    DistributorResponse updated = service.updateDistributor(id, distributorRequest);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> removeDistributor(@PathVariable Long id) {
    service.removeDistributor(id);
    return ResponseEntity.ok().build();
  }
}
