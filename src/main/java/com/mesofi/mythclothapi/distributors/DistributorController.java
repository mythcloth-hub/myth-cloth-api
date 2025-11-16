package com.mesofi.mythclothapi.distributors;

import com.mesofi.mythclothapi.distributors.model.DistributorRequest;
import com.mesofi.mythclothapi.distributors.model.DistributorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/distributors")
@RequiredArgsConstructor
public class DistributorController {

  private final DistributorService distributorService;

  @PostMapping
  public ResponseEntity<Void> createDistributor(
      @RequestBody DistributorRequest distributorRequest) {
    distributorService.createDistributor(distributorRequest);
    return ResponseEntity.accepted().build();
  }

  @GetMapping
  public List<DistributorResponse> retrieveDistributors() {
    return distributorService.retrieveDistributors();
  }
}
