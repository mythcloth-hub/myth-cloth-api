package com.mesofi.mythclothapi.security.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SecurityDataInitializerTest {

    @Mock
    private SecurityDataService securityDataService;

    @InjectMocks
    private SecurityDataInitializer securityDataInitializer;

    @Test
    void initializeSecurityData_shouldDelegateToSecurityDataService() {
        securityDataInitializer.initializeSecurityData();

        verify(securityDataService).initializeSecurityData();
    }
}
