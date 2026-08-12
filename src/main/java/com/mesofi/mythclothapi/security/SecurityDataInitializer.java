package com.mesofi.mythclothapi.security;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 
 * Initializes the application's security data when the application is ready.
 *
 * <p>
 * This component listens for the {@link ApplicationReadyEvent}, which is
 * published after the application context has been fully initialized. When the
 * event is received, it delegates the initialization of roles, permissions, and
 * their relationships to {@link SecurityDataService}.
 * </p>
 * 
 */
@Component
@RequiredArgsConstructor
public class SecurityDataInitializer {

    private final SecurityDataService securityDataService;

    /**
     * Initializes the application's security data after the application has started
     * successfully.
     *
     * <p>
     * The initialization is executed within a transaction to ensure that the
     * security data is persisted consistently.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeSecurityData() {
        securityDataService.initializeSecurityData();
    }
}
