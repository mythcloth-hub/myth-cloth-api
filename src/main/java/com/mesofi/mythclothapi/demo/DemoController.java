package com.mesofi.mythclothapi.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller exposing the application's demo configuration.
 */
@Slf4j
@RestController
@RequestMapping("/demos")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService service;

    /**
     * Retrieves the current demo mode status.
     *
     * @return the current demo configuration
     */
    @GetMapping
    @Operation(summary = "Get demo status", description = "Returns whether demo mode is currently enabled.")
    public DemoResp getDemoStatus() {
        DemoResp demoResp = service.getDemoStatus();
        log.info("Demo enabled: {}", demoResp.enabled());
        return demoResp;
    }
}
