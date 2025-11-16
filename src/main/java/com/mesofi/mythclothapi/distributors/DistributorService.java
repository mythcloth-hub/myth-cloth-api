package com.mesofi.mythclothapi.distributors;

import com.mesofi.mythclothapi.distributors.model.DistributorRequest;
import com.mesofi.mythclothapi.distributors.model.DistributorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributorService {

  private final DistributorRepository distributorRepository;
  private final DistributorMapper mapper;

  public void createDistributor(DistributorRequest distributorRequest) {
    distributorRepository.save(mapper.toDistributorEntity(distributorRequest));
  }

  public List<DistributorResponse> retrieveDistributors() {
    return distributorRepository.findAll().stream().map(mapper::toDistributorResponse).toList();
  }
}
