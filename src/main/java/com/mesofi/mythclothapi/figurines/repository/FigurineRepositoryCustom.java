package com.mesofi.mythclothapi.figurines.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.model.Figurine;

public interface FigurineRepositoryCustom {
  Page<Figurine> findPaginated(FigurineFilter filter, Pageable pageable);

  List<Figurine> findAll(FigurineFilter filter);

  List<Figurine> findAllByYear(int year);
}
