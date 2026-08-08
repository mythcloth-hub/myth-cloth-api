package com.mesofi.mythclothapi.security;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityDataInitializer {

    private final SecurityDataService securityDataService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialize() {
        securityDataService.initialize();
    }

}
