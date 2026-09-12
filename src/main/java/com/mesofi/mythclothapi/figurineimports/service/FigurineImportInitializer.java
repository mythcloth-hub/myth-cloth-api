package com.mesofi.mythclothapi.figurineimports.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Initializes the figurine import process when the application is ready.
 *
 * <p>
 * This component listens for the {@link ApplicationReadyEvent}, which is
 * published after the application context has been fully initialized. When the
 * event is received, it delegates the import of figurines from a public drive
 * to {@link FigurineImportService}.
 * </p>
 *
 * <p>
 * This initializer is only active when the "local" profile is active, allowing
 * for environment-specific behavior.
 * </p>
 */
@Component
@Profile("local")
@RequiredArgsConstructor
public class FigurineImportInitializer {

    private final FigurineImportService figurineImportService;

    /**
     * Initializes the figurine import process after the application has started
     * successfully.
     *
     * <p>
     * The import process is executed when the application is ready, ensuring that
     * all necessary components and configurations are in place.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        figurineImportService.importAllFigurinesFromPublicDrive();
    }
}
