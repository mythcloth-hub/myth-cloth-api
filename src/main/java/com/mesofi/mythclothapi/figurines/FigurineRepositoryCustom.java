package com.mesofi.mythclothapi.figurines;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mesofi.mythclothapi.figurines.model.Figurine;

public interface FigurineRepositoryCustom {
  Page<Figurine> search(FigurineFilter filter, Pageable pageable);
}
