package com.mesofi.mythclothapi.references;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.references.model.ReferencePairRequest;
import com.mesofi.mythclothapi.references.model.ReferencePairResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ReferencePairController {

  private final ReferencePairService referencePairService;

  @PostMapping("/{referenceName}")
  public ResponseEntity<ReferencePairResponse> createDistributor(
      @PathVariable String referenceName, @Valid @RequestBody ReferencePairRequest request) {

    ReferencePairResponse response = referencePairService.createEntry(referenceName, request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest() // e.g. /api/series
            .path("/{id}") // append /{id}
            .buildAndExpand(response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }
}
