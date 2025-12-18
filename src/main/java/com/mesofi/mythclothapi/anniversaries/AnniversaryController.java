package com.mesofi.mythclothapi.anniversaries;

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

import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryReq;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/anniversaries")
@RequiredArgsConstructor
public class AnniversaryController {

  private final AnniversaryService service;

  @PostMapping
  public ResponseEntity<AnniversaryResp> createAnniversary(
      @Valid @RequestBody AnniversaryReq anniversaryRequest) {
    AnniversaryResp response = service.createAnniversary(anniversaryRequest);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest() // e.g. /api/anniversaries
            .path("/{id}") // append /{id}
            .buildAndExpand(response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  public AnniversaryResp retrieveAnniversary(@PathVariable Long id) {
    return service.retrieveAnniversary(id);
  }

  @GetMapping
  public List<AnniversaryResp> retrieveAnniversaries() {
    return service.retrieveAnniversaries();
  }

  @PutMapping("/{id}")
  public ResponseEntity<AnniversaryResp> updateAnniversary(
      @PathVariable Long id, @Valid @RequestBody AnniversaryReq anniversaryRequest) {
    AnniversaryResp updated = service.updateAnniversary(id, anniversaryRequest);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> removeAnniversary(@PathVariable Long id) {
    service.removeAnniversary(id);
    return ResponseEntity.ok().build();
  }
}
