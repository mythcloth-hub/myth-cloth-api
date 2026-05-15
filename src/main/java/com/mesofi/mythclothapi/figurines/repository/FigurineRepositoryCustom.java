package com.mesofi.mythclothapi.figurines.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.model.Figurine;

public interface FigurineRepositoryCustom {
  Page<Figurine> search(FigurineFilter filter, Pageable pageable);
}
