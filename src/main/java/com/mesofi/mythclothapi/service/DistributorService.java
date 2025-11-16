package com.mesofi.mythclothapi.service;

import com.mesofi.mythclothapi.mapper.DistributorMapper;
import com.mesofi.mythclothapi.model.DistributorRequest;
import com.mesofi.mythclothapi.model.DistributorResponse;
import com.mesofi.mythclothapi.repository.DistributorRepository;
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
