package com.mesofi.mythclothapi.demo;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Service for retrieving the current demo configuration.
 *
 * <p>
 * Provides access to the application's demo settings exposed through
 * {@link DemoProperties}.
 */
@Service
@RequiredArgsConstructor
public class DemoService {

    private final DemoProperties demoProperties;
    /**
     * Returns whether demo mode is currently enabled.
     *
     * @return a response containing the demo mode status
     */
    public DemoResp getDemoStatus() {
        return new DemoResp(Optional.ofNullable(demoProperties.enabled()).orElse(false),
                demoProperties.providerUserId(), demoProperties.name(), demoProperties.email());
    }
}
