package com.mesofi.mythclothapi.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemoServiceTest {

    @Mock
    private DemoProperties demoProperties;

    @InjectMocks
    private DemoService service;

    @Test
    void getDemoStatus_shouldDefaultEnabledToFalse_whenPropertyIsNull() {
        when(demoProperties.enabled()).thenReturn(null);
        when(demoProperties.providerUserId()).thenReturn("demo-user");
        when(demoProperties.name()).thenReturn("Demo User");
        when(demoProperties.email()).thenReturn("demo@example.com");

        DemoResp response = service.getDemoStatus();

        assertThat(response.enabled()).isFalse();
        assertThat(response.providerUserId()).isEqualTo("demo-user");
        assertThat(response.name()).isEqualTo("Demo User");
        assertThat(response.email()).isEqualTo("demo@example.com");
    }
}
