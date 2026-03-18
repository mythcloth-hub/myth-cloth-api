package com.mesofi.mythclothapi.anniversaries;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryReq;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnniversaryService {

  private final AnniversaryRepository repository;
  private final AnniversaryMapper mapper;

  @Transactional
  public AnniversaryResp createAnniversary(AnniversaryReq request) {
    log.info("Creating anniversary: {} - {}", request.description(), request.year());

    Anniversary entity = mapper.toAnniversary(request);

    var saved = repository.save(entity);
    return mapper.toAnniversaryResp(saved);
  }

  @Transactional(readOnly = true)
  public AnniversaryResp retrieveAnniversary(Long id) {
    return repository
        .findById(id)
        .map(mapper::toAnniversaryResp)
        .orElseThrow(() -> new AnniversaryNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public List<AnniversaryResp> retrieveAnniversaries() {
    return repository.findAll().stream().map(mapper::toAnniversaryResp).toList();
  }

  @Transactional
  public AnniversaryResp updateAnniversary(Long id, AnniversaryReq request) {
    log.info("Updating anniversary {} to {}", id, request.description());
    var existing = repository.findById(id).orElseThrow(() -> new AnniversaryNotFoundException(id));

    existing.setDescription(request.description());
    existing.setYear(request.year());

    var saved = repository.save(existing);
    return mapper.toAnniversaryResp(saved);
  }

  public void removeAnniversary(Long id) {
    log.warn("Removing anniversary {}", id);

    if (!repository.existsById(id)) {
      throw new AnniversaryNotFoundException(id);
    }
    repository.deleteById(id);
  }
}
