package com.mesofi.mythclothapi.collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/collectors/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CollectorController {

  private final CollectorService collectorService;

  @PostMapping("/{provider}")
  public CollectorLoginResp login(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String provider,
      @RequestBody @Validated FacebookLoginReq facebookLoginRequest) {
    // log.info("subject={}", jwt.getSubject());
    // log.info("email={}", jwt.getClaimAsString("email"));
    // log.info("Issuer={}", jwt.getIssuer());

    // return collectorService.login(jwt);
    return collectorService.login(facebookLoginRequest.getAccessToken());
  }
}
