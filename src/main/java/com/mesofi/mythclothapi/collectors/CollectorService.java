package com.mesofi.mythclothapi.collectors;

import static com.mesofi.mythclothapi.collectorproviders.ProviderType.FACEBOOK;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.collectorproviders.CollectorAuthProvider;
import com.mesofi.mythclothapi.collectorproviders.CollectorAuthProviderRepository;
import com.mesofi.mythclothapi.collectorproviders.ProviderType;
import com.mesofi.mythclothapi.integration.fb.FbApiClient;
import com.mesofi.mythclothapi.integration.fb.FbTokenData;
import com.mesofi.mythclothapi.integration.fb.FbUserInfoResponse;
import com.mesofi.mythclothapi.integration.fb.FcCredentialsProperties;
import com.mesofi.mythclothapi.security.ApiTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class CollectorService {

  private final CollectorRepository collectorRepository;
  private final CollectorAuthProviderRepository collectorAuthProviderRepository;
  private final FbApiClient fbApiClient;
  private final FcCredentialsProperties fcCredentials;
  private final ApiTokenService apiTokenService;

  public CollectorLoginResp login(String accessToken) {

    FbTokenData fbTokenData = fbApiClient.validateAccessToken(accessToken).data();
    if (fbTokenData.valid() && fbTokenData.appId().equals(fcCredentials.appId())) {
      // the token is valid, now we get the user profile...
      log.info("Facebook token is valid for user id {}", fbTokenData.userId());
      FbUserInfoResponse fbUserInfoResponse = fbApiClient.getUserInfo(accessToken);
      log.info(
          "Facebook user info: id={}, name={}, email={}",
          fbUserInfoResponse.id(),
          fbUserInfoResponse.name(),
          fbUserInfoResponse.email());

      // creates or update user
      Collector collector =
          createOrUpdateRegisteredCollector(
              FACEBOOK,
              fbUserInfoResponse.id(),
              fbUserInfoResponse.name(),
              fbUserInfoResponse.email(),
              true,
              null);

      String apiJwt =
          apiTokenService.generateToken(
              collector.getId(), FACEBOOK.name(), fbUserInfoResponse.id(), collector.getEmail());

      return new CollectorLoginResp(
          collector.getId(),
          collector.getDisplayName(),
          collector.getEmail(),
          apiJwt,
          "Bearer",
          3600L);
    }
    throw new CollectorInvalidTokenException("Facebook token is invalid");
  }

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
    } else {
      // no existing provider, create new collector and provider
      log.info("Creating new collector for {} user {}", providerType, userId);

      Collector collector = new Collector();
      collector.setEmail(email);
      collector.setDisplayName(name);
      collector.setProfilePictureUrl(picture);
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
  }

  public CollectorLoginResp login(Jwt jwt) {

    URL issuer = jwt.getIssuer();
    try {
      if (issuer.toURI().equals(new URI("https://accounts.google.com"))) {
        String subject = jwt.getSubject();

        Optional<CollectorAuthProvider> found =
            collectorAuthProviderRepository.findByProviderAndProviderUserId(
                ProviderType.GOOGLE, subject);

        if (found.isPresent()) {
          Collector collector = found.get().getCollector();

          log.info("Logging in collector {} with Google provider", collector.getId());

          return new CollectorLoginResp(
              collector.getId(), collector.getDisplayName(), collector.getEmail(), null, null, 0);
        } else {
          // no existing provider, create new collector and provider
          log.info("Creating new collector for Google user {}", subject);

          Collector collector = new Collector();
          collector.setEmail(jwt.getClaimAsString("email"));
          collector.setDisplayName(jwt.getClaimAsString("name"));
          collector.setProfilePictureUrl(jwt.getClaimAsString("picture"));
          Collector collectorSaved = collectorRepository.save(collector);

          CollectorAuthProvider newProvider = new CollectorAuthProvider();
          newProvider.setCollector(collectorSaved);
          newProvider.setProvider(ProviderType.GOOGLE);
          newProvider.setEmail(jwt.getClaimAsString("email"));
          newProvider.setProviderUserId(subject);
          newProvider.setEmailVerified(jwt.getClaimAsBoolean("email_verified"));
          collectorAuthProviderRepository.save(newProvider);

          return new CollectorLoginResp(
              collectorSaved.getId(),
              collectorSaved.getDisplayName(),
              collectorSaved.getEmail(),
              null,
              null,
              0);
        }
      }
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return null;
  }
}
