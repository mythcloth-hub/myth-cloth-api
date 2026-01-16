package com.mesofi.mythclothapi.figurines;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mesofi.mythclothapi.figurines.dto.FigurineReq;
import com.mesofi.mythclothapi.figurines.dto.FigurineResp;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/figurines")
@RequiredArgsConstructor
public class FigurineController {

  private final FigurineService service;

  @PostMapping
  public ResponseEntity<FigurineResp> createFigurine(
      @RequestBody @Valid FigurineReq figurineRequest) {

    FigurineResp response = service.createFigurine(figurineRequest);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}") // append /{id}
            .buildAndExpand(response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<FigurineResp> updateFigurine(
      @PathVariable Long id, @RequestBody @Valid FigurineReq figurineRequest) {
    FigurineResp updated = service.updateFigurine(id, figurineRequest);
    return ResponseEntity.ok(updated);
  }
}
