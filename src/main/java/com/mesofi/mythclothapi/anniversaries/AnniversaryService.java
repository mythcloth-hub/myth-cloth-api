package com.mesofi.mythclothapi.anniversaries;

import static com.mesofi.mythclothapi.catalogs.CatalogService.CATALOG_CONTEXT_CACHE;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryReq;
import com.mesofi.mythclothapi.anniversaries.dto.AnniversaryResp;
import com.mesofi.mythclothapi.anniversaries.model.Anniversary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnniversaryService {

    private final AnniversaryRepository repository;
    private final AnniversaryMapper mapper;

    @Transactional
    @CacheEvict(value = CATALOG_CONTEXT_CACHE, allEntries = true)
    public AnniversaryResp createAnniversary(AnniversaryReq request) {
        log.info("Creating anniversary: {} - {}", request.description(), request.year());

        Anniversary entity = mapper.toAnniversary(request);

        var saved = repository.save(entity);
        return mapper.toAnniversaryResp(saved);
    }

    @Transactional(readOnly = true)
    public AnniversaryResp retrieveAnniversary(Long id) {
        return repository.findById(id).map(mapper::toAnniversaryResp)
                .orElseThrow(() -> new AnniversaryNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<AnniversaryResp> retrieveAnniversaries() {
        return repository.findAll(Sort.by("id")).stream().map(mapper::toAnniversaryResp).toList();
    }

    @Transactional
    @CacheEvict(value = CATALOG_CONTEXT_CACHE, allEntries = true)
    public AnniversaryResp updateAnniversary(Long id, AnniversaryReq request) {
        log.info("Updating anniversary {} to {}", id, request.description());
        var existing = repository.findById(id).orElseThrow(() -> new AnniversaryNotFoundException(id));

        existing.setName(request.description());
        existing.setYear(request.year());
        existing.setType(request.type());

        var saved = repository.save(existing);
        return mapper.toAnniversaryResp(saved);
    }

    @Transactional
    @CacheEvict(value = CATALOG_CONTEXT_CACHE, allEntries = true)
    public void removeAnniversary(Long id) {
        log.warn("Removing anniversary {}", id);

        if (!repository.existsById(id)) {
            throw new AnniversaryNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
