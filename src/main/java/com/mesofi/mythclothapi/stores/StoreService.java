package com.mesofi.mythclothapi.stores;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesofi.mythclothapi.stores.dto.StoreReq;
import com.mesofi.mythclothapi.stores.dto.StoreResp;
import com.mesofi.mythclothapi.stores.model.Store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository repository;
    private final StoreMapper mapper;

    @Transactional
    public StoreResp createStore(StoreReq request) {
        log.info("Creating store: {} - {}", request.name(), request.storeName());

        Store entity = mapper.toStore(request);

        var saved = repository.save(entity);
        return mapper.toStoreResp(saved);
    }
}
