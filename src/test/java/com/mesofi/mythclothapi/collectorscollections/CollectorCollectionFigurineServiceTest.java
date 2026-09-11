package com.mesofi.mythclothapi.collectorscollections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mesofi.mythclothapi.collectors.CollectorRepository;
import com.mesofi.mythclothapi.collectors.mapper.CollectorMapper;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionFigurineRepository;
import com.mesofi.mythclothapi.collectorscollections.repository.CollectorCollectionRepository;
import com.mesofi.mythclothapi.figurines.repository.FigurineRepository;

@ExtendWith(MockitoExtension.class)
class CollectorCollectionFigurineServiceTest {

    @InjectMocks
    private CollectorCollectionFigurineService service;

    @Mock
    private CollectorCollectionFigurineRepository collectorCollectionFigurineRepository;
    @Mock
    private CollectorCollectionRepository collectorCollectionRepository;
    @Mock
    private CollectorRepository collectorRepository;
    @Mock
    private FigurineRepository figurineRepository;
    @Mock
    private CollectorMapper collectorMapper;

    @Test
    void addFigurineToCollection_shouldThrowIllegalArgumentException_whenFigurineDoesNotExist() {

    }

}
