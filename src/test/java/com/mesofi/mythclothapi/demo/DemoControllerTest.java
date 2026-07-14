package com.mesofi.mythclothapi.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemoControllerTest {

    @Mock
    private DemoService service;

    @InjectMocks
    private DemoController controller;

    @Test
    void getDemoStatus_shouldReturnServiceResponse() {
        DemoResp expected = new DemoResp(true, "demo-user", "Demo User", "demo@example.com");
        when(service.getDemoStatus()).thenReturn(expected);

        DemoResp response = controller.getDemoStatus();

        assertThat(response).isEqualTo(expected);
        verify(service).getDemoStatus();
    }
}
