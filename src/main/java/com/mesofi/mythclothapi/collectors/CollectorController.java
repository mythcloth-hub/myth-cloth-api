package com.mesofi.mythclothapi.collectors;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mesofi.mythclothapi.collectors.dto.CollectorLoginReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginResp;
import com.mesofi.mythclothapi.collectors.dto.CollectorSignupReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorSignupResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller that handles collector social-auth login requests.
 *
 * <p>
 * Exposes authentication endpoints under {@code /collectors/auth} and delegates
 * login orchestration to {@link CollectorService}.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/collectors")
@RequiredArgsConstructor
public class CollectorController {

    private final CollectorService collectorService;

    /**
     * Authenticates a collector using the specified social provider.
     *
     * @param provider
     *            social login provider identifier (for example, google or facebook)
     * @param loginRequest
     *            social login payload containing provider-issued authentication
     *            data
     * @return the collector login response with authenticated collector details
     */
    @PostMapping("/auth/{provider}")
    public CollectorLoginResp login(@PathVariable String provider,
            @RequestBody @Validated CollectorLoginReq loginRequest) {
        return collectorService.login(provider, loginRequest);
    }

    /**
     * Registers a new collector account using the provided signup request.
     *
     * @param signupRequest
     *            the collector signup request containing necessary information
     * @return the collector signup response with registered collector details
     */
    @PostMapping("/signup")
    public CollectorSignupResp signup(@RequestBody @Validated CollectorSignupReq signupRequest) {
        return collectorService.signup(signupRequest);
    }
}
