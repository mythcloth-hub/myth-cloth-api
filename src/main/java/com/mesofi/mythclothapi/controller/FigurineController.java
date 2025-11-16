package com.mesofi.mythclothapi.controller;

import com.mesofi.mythclothapi.service.FigurineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/figurines")
@RequiredArgsConstructor
public class FigurineController {

  private final FigurineService figurineService;

  @PostMapping("/import/{fileId}")
  public HttpStatus importFromDrive(@PathVariable String fileId) {
    int count = figurineService.importFromPublicDrive(fileId);
    log.info("Imported {} figurines successfully", count);
    return HttpStatus.ACCEPTED;
  }

  // @PostMapping
  // public ResponseEntity<Figurine> createFigurine(@RequestBody Figurine figurine) { ... }

  // @PutMapping("/{id}")
  // public ResponseEntity<Figurine> updateFigurine(@PathVariable Long id, @RequestBody Figurine
  // figurine) { ... }
}
