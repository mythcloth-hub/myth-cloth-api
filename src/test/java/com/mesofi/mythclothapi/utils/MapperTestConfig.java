package com.mesofi.mythclothapi.utils;

import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.mesofi.mythclothapi.figurineevents.FigurineEventMapper;
import com.mesofi.mythclothapi.figurines.mapper.FigurineMapper;

@TestConfiguration
public class MapperTestConfig {

  @Bean
  FigurineMapper figurineMapper() {
    return Mappers.getMapper(FigurineMapper.class);
  }

  @Bean
  FigurineEventMapper figurineEventMapper() {
    return Mappers.getMapper(FigurineEventMapper.class);
  }
}
