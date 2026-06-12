package com.mesofi.mythclothapi.collectors;

import static com.mesofi.mythclothapi.collectorproviders.model.ProviderType.FACEBOOK;
import static com.mesofi.mythclothapi.collectorproviders.model.ProviderType.GOOGLE;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.collectorproviders.CollectorAuthProviderRepository;
import com.mesofi.mythclothapi.collectorproviders.model.CollectorAuthProvider;
import com.mesofi.mythclothapi.collectorproviders.model.ProviderType;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginResp;
import com.mesofi.mythclothapi.integration.fb.FbApiClient;
import com.mesofi.mythclothapi.integration.fb.FbTokenData;
import com.mesofi.mythclothapi.integration.fb.FbUserInfoResponse;
import com.mesofi.mythclothapi.integration.fb.FcCredentialsProperties;
import com.mesofi.mythclothapi.integration.google.GoogleApiClient;
import com.mesofi.mythclothapi.integration.google.GoogleCredentialsProperties;
import com.mesofi.mythclothapi.integration.google.GoogleTokenInfoResponse;
import com.mesofi.mythclothapi.security.ApiTokenService;
import com.mesofi.mythclothapi.security.roles.RoleRepository;
import com.mesofi.mythclothapi.security.roles.model.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service that orchestrates collector social authentication and login.
 *
 * <p>Validates provider tokens, creates or reuses collector/provider associations, and returns the
 * API authentication payload used by the client.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class CollectorService {

  private final CollectorRepository collectorRepository;
  private final CollectorAuthProviderRepository collectorAuthProviderRepository;
  private final FbApiClient fbApiClient;
  private final FcCredentialsProperties fcCredentials;
  private final GoogleApiClient googleApiClient;
  private final GoogleCredentialsProperties googleCredentials;
  private final ApiTokenService apiTokenService;
  private final RoleRepository roleRepository;

  /**
   * Logs in a collector using the requested social provider.
   *
   * @param provider provider name from the API path (for example, facebook or google)
   * @param loginRequest social login payload with provider token values
   * @return authenticated collector response containing API token details
   * @throws IllegalArgumentException if provider is missing or unsupported
   */
  public CollectorLoginResp login(String provider, CollectorLoginReq loginRequest) {
    ProviderType providerType = resolveProvider(provider);

    return switch (providerType) {
      case FACEBOOK -> loginWithFacebook(loginRequest.accessToken());
      case GOOGLE -> loginWithGoogle(loginRequest.idToken());
      default ->
          throw new IllegalArgumentException(
              "Provider %s is not supported yet".formatted(providerType));
    };
  }

  /**
   * Validates a Facebook access token and logs in or provisions the related collector.
   *
   * @param accessToken Facebook user access token
   * @return authenticated collector response containing API token details
   * @throws IllegalArgumentException if the token is blank
   * @throws CollectorInvalidTokenException if the token is invalid for this application
   */
  private CollectorLoginResp loginWithFacebook(String accessToken) {
    requireToken(accessToken, "Facebook access token is required");

    FbTokenData fbTokenData = fbApiClient.validateAccessToken(accessToken).data();
    boolean appMatches = fcCredentials.appId().equals(fbTokenData.appId());

    if (!fbTokenData.valid() || !appMatches) {
      throw new CollectorInvalidTokenException("Facebook token is invalid");
    }

    FbUserInfoResponse userInfo = fbApiClient.getUserInfo(accessToken);

    Collector collector =
        createOrUpdateRegisteredCollector(
            FACEBOOK, userInfo.id(), userInfo.name(), userInfo.email(), true, null);

    return buildLoginResponse(collector, FACEBOOK, userInfo.id());
  }

  /**
   * Validates a Google ID token and logs in or provisions the related collector.
   *
   * @param idToken Google ID token
   * @return authenticated collector response containing API token details
   * @throws IllegalArgumentException if the token is blank
   * @throws CollectorInvalidTokenException if token claims are invalid or expired
   */
  private CollectorLoginResp loginWithGoogle(String idToken) {
    requireToken(idToken, "Google idToken is required");

    GoogleTokenInfoResponse tokenInfo = googleApiClient.validateIdToken(idToken);
    validateGoogleToken(tokenInfo);

    Collector collector =
        createOrUpdateRegisteredCollector(
            GOOGLE,
            tokenInfo.sub(),
            tokenInfo.name(),
            tokenInfo.email(),
            tokenInfo.emailVerified(),
            tokenInfo.picture());

    return buildLoginResponse(collector, GOOGLE, tokenInfo.sub());
  }

  /**
   * Verifies Google token claims required by this API.
   *
   * @param tokenInfo parsed token info returned by Google token introspection
   * @throws CollectorInvalidTokenException if issuer, audience, expiry, or subject is invalid
   */
  private void validateGoogleToken(GoogleTokenInfoResponse tokenInfo) {
    boolean issuerValid =
        "https://accounts.google.com".equals(tokenInfo.iss())
            || "accounts.google.com".equals(tokenInfo.iss());

    if (!issuerValid) {
      throw new CollectorInvalidTokenException("Google token issuer is invalid");
    }

    if (!googleCredentials.clientId().equals(tokenInfo.aud())) {
      throw new CollectorInvalidTokenException("Google token audience is invalid");
    }

    long expiresAt;
    try {
      expiresAt = tokenInfo.expiresAtEpochSecond();
    } catch (NumberFormatException ex) {
      throw new CollectorInvalidTokenException("Google token expiry claim is invalid");
    }

    if (expiresAt <= Instant.now().getEpochSecond()) {
      throw new CollectorInvalidTokenException("Google token is expired");
    }

    if (tokenInfo.sub() == null || tokenInfo.sub().isBlank()) {
      throw new CollectorInvalidTokenException("Google token subject is missing");
    }
  }

  /**
   * Builds the API login response and signs an internal API token for the collector.
   *
   * @param collector authenticated collector entity
   * @param provider social provider used for the login
   * @param providerUserId provider-specific user id
   * @return login response payload for API clients
   */
  private CollectorLoginResp buildLoginResponse(
      Collector collector, ProviderType provider, String providerUserId) {
    String apiJwt =
        apiTokenService.generateToken(
            collector, provider.name(), providerUserId, collector.getEmail());

    return new CollectorLoginResp(
        collector.getId(),
        collector.getDisplayName(),
        collector.getEmail(),
        apiJwt,
        "Bearer",
        apiTokenService.ttlSeconds());
  }

  /**
   * Resolves an incoming provider string to a supported {@link ProviderType}.
   *
   * @param provider raw provider value from the request path
   * @return normalized provider enum value
   * @throws IllegalArgumentException if provider is missing or unsupported
   */
  private ProviderType resolveProvider(String provider) {
    if (provider == null || provider.isBlank()) {
      throw new IllegalArgumentException("Provider is required");
    }

    try {
      return ProviderType.valueOf(provider.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Unsupported provider: " + provider);
    }
  }

  /**
   * Ensures a required token value is present.
   *
   * @param token token value to validate
   * @param message error message used when token is missing
   * @throws IllegalArgumentException if token is null or blank
   */
  private void requireToken(String token, String message) {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Finds an existing collector by provider identity or creates a new collector and link.
   *
   * @param providerType provider associated with the social identity
   * @param userId provider user id
   * @param name display name from provider claims
   * @param email email from provider claims
   * @param emailVerified provider-reported email verification flag
   * @param picture profile picture URL from provider claims
   * @return existing or newly created collector entity
   */
  private Collector createOrUpdateRegisteredCollector(
      ProviderType providerType,
      String userId,
      String name,
      String email,
      boolean emailVerified,
      String picture) {
    Optional<CollectorAuthProvider> found =
        collectorAuthProviderRepository.findByProviderAndProviderUserId(providerType, userId);
    if (found.isPresent()) {
      Collector existingCollector = found.get().getCollector();

      log.info("Logging in collector {} with {} provider", existingCollector.getId(), providerType);

      return existingCollector;
    }

    // In case it is the first collector, this would have an Admin role.
    String role = collectorRepository.count() == 0 ? "Admin" : "Basic Collector";
    Role currRole = retrieveRoleByDescription(role);

    // No existing provider link, create collector and provider association.
    log.info("Creating new collector for {} user {}", providerType, userId);

    Collector collector = new Collector();
    collector.setEmail(email);
    collector.setDisplayName(name);
    collector.setProfilePictureUrl(picture);
    collector.setRole(currRole);
    Collector newCollector = collectorRepository.save(collector);

    CollectorAuthProvider newProvider = new CollectorAuthProvider();
    newProvider.setCollector(newCollector);
    newProvider.setProvider(providerType);
    newProvider.setEmail(email);
    newProvider.setProviderUserId(userId);
    newProvider.setEmailVerified(emailVerified);
    collectorAuthProviderRepository.save(newProvider);

    return newCollector;
  }

  private Role retrieveRoleByDescription(String description) {
    return roleRepository
        .findByDescription(description)
        .orElseGet(
            () -> {
              Role adminRole = new Role();
              adminRole.setDescription(description);
              return roleRepository.save(adminRole);
            });
  }
}
