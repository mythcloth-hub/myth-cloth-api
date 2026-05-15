package com.mesofi.mythclothapi.stats;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import com.mesofi.mythclothapi.figurines.FigurineFilter;
import com.mesofi.mythclothapi.figurines.model.Figurine;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsService {

  private final FigurineRepository repository;

  public String test(@NotNull FigurineFilter filter) {

    List<Figurine> sd = repository.findAllFigurines(filter);
    System.out.println(sd.size());

    return "ddd";
  }
}
