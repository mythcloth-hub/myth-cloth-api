package com.mesofi.mythclothapi.collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mesofi.mythclothapi.collectorproviders.CollectorAuthProviderRepository;
import com.mesofi.mythclothapi.collectorproviders.model.CollectorAuthProvider;
import com.mesofi.mythclothapi.collectorproviders.model.ProviderType;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginResp;
import com.mesofi.mythclothapi.integration.fb.FbApiClient;
import com.mesofi.mythclothapi.integration.fb.FbTokenData;
import com.mesofi.mythclothapi.integration.fb.FbTokenResponse;
import com.mesofi.mythclothapi.integration.fb.FbUserInfoResponse;
import com.mesofi.mythclothapi.integration.fb.FcCredentialsProperties;
import com.mesofi.mythclothapi.integration.google.GoogleApiClient;
import com.mesofi.mythclothapi.integration.google.GoogleCredentialsProperties;
import com.mesofi.mythclothapi.integration.google.GoogleTokenInfoResponse;
import com.mesofi.mythclothapi.security.ApiTokenService;
import com.mesofi.mythclothapi.security.roles.RoleRepository;
import com.mesofi.mythclothapi.security.roles.model.Role;

@ExtendWith(MockitoExtension.class)
class CollectorServiceTest {

  @InjectMocks private CollectorService service;

  @Mock private CollectorRepository collectorRepository;
  @Mock private CollectorAuthProviderRepository collectorAuthProviderRepository;
  @Mock private FbApiClient fbApiClient;
  @Mock private FcCredentialsProperties fcCredentials;
  @Mock private GoogleApiClient googleApiClient;
  @Mock private GoogleCredentialsProperties googleCredentials;
  @Mock private ApiTokenService apiTokenService;
  @Mock private RoleRepository roleRepository;

  @Test
  void login_shouldThrowIllegalArgumentException_whenProviderIsBlank() {
    CollectorLoginReq request = new CollectorLoginReq("id-token", "access-token");

    assertThatThrownBy(() -> service.login("   ", request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provider is required");
  }

  @Test
  void login_shouldThrowIllegalArgumentException_whenProviderIsNull() {
    CollectorLoginReq request = new CollectorLoginReq("id-token", "access-token");

    assertThatThrownBy(() -> service.login(null, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provider is required");
  }

  @Test
  void login_shouldThrowIllegalArgumentException_whenProviderIsUnknown() {
    CollectorLoginReq request = new CollectorLoginReq("id-token", "access-token");

    assertThatThrownBy(() -> service.login("twitter", request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unsupported provider: twitter");
  }

  @Test
  void login_shouldThrowIllegalArgumentException_whenProviderIsNotSupportedYet() {
    CollectorLoginReq request = new CollectorLoginReq("id-token", "access-token");

    assertThatThrownBy(() -> service.login("github", request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provider GITHUB is not supported yet");
  }

  @Test
  void loginWithFacebook_shouldThrowIllegalArgumentException_whenAccessTokenIsMissing() {
    CollectorLoginReq request = new CollectorLoginReq(null, "   ");

    assertThatThrownBy(() -> service.login("facebook", request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Facebook access token is required");
  }

  @Test
  void loginWithFacebook_shouldThrowCollectorInvalidTokenException_whenTokenIsInvalid() {
    CollectorLoginReq request = new CollectorLoginReq(null, "fb-access-token");

    when(fcCredentials.appId()).thenReturn("myth-app-id");
    when(fbApiClient.validateAccessToken("fb-access-token"))
        .thenReturn(new FbTokenResponse(fbTokenData("myth-app-id", false, "user-1")));

    assertThatThrownBy(() -> service.login("facebook", request))
        .isInstanceOf(CollectorInvalidTokenException.class)
        .hasMessage("Facebook token is invalid");
  }

  @Test
  void loginWithFacebook_shouldThrowCollectorInvalidTokenException_whenAppIdDoesNotMatch() {
    CollectorLoginReq request = new CollectorLoginReq(null, "fb-access-token");

    when(fcCredentials.appId()).thenReturn("myth-app-id");
    when(fbApiClient.validateAccessToken("fb-access-token"))
        .thenReturn(new FbTokenResponse(fbTokenData("another-app-id", true, "user-1")));

    assertThatThrownBy(() -> service.login("facebook", request))
        .isInstanceOf(CollectorInvalidTokenException.class)
        .hasMessage("Facebook token is invalid");
  }

  @Test
  void loginWithFacebook_shouldReuseExistingCollector_whenProviderLinkExists() {
    Collector existingCollector = collector(7L, "Ikki", "ikki@example.com", null);
    CollectorAuthProvider providerLink =
        providerLink(existingCollector, ProviderType.FACEBOOK, "fb-777");

    when(fcCredentials.appId()).thenReturn("myth-app-id");
    when(fbApiClient.validateAccessToken("fb-access-token"))
        .thenReturn(new FbTokenResponse(fbTokenData("myth-app-id", true, "fb-777")));
    when(fbApiClient.getUserInfo("fb-access-token"))
        .thenReturn(new FbUserInfoResponse("fb-777", "Phoenix Ikki", "ikki@example.com"));
    when(collectorAuthProviderRepository.findByProviderAndProviderUserId(
            ProviderType.FACEBOOK, "fb-777"))
        .thenReturn(Optional.of(providerLink));
    when(apiTokenService.generateToken(7L, "FACEBOOK", "fb-777", "ikki@example.com"))
        .thenReturn("api-jwt");
    when(apiTokenService.ttlSeconds()).thenReturn(3600L);

    CollectorLoginResp response =
        service.login("facebook", new CollectorLoginReq(null, "fb-access-token"));

    assertThat(response)
        .extracting(
            CollectorLoginResp::collectorId,
            CollectorLoginResp::displayName,
            CollectorLoginResp::email,
            CollectorLoginResp::accessToken,
            CollectorLoginResp::tokenType,
            CollectorLoginResp::expiresInSeconds)
        .containsExactly(7L, "Ikki", "ikki@example.com", "api-jwt", "Bearer", 3600L);

    verify(collectorRepository, never()).save(any(Collector.class));
    verify(collectorAuthProviderRepository, never()).save(any(CollectorAuthProvider.class));
  }

  @Test
  void loginWithFacebook_shouldCreateCollectorAndProvider_whenProviderLinkDoesNotExist() {
    when(fcCredentials.appId()).thenReturn("myth-app-id");
    when(fbApiClient.validateAccessToken("fb-access-token"))
        .thenReturn(new FbTokenResponse(fbTokenData("myth-app-id", true, "fb-123")));
    when(fbApiClient.getUserInfo("fb-access-token"))
        .thenReturn(new FbUserInfoResponse("fb-123", "Seiya", "seiya@example.com"));
    when(collectorAuthProviderRepository.findByProviderAndProviderUserId(
            ProviderType.FACEBOOK, "fb-123"))
        .thenReturn(Optional.empty());
    when(collectorRepository.count()).thenReturn(5L);

    Collector savedCollector = collector(11L, "Seiya", "seiya@example.com", null);
    Role basicRole = new Role();
    basicRole.setId(2L);
    basicRole.setDescription("Basic Collector");
    when(roleRepository.findByDescription("Basic Collector")).thenReturn(Optional.of(basicRole));
    when(collectorRepository.save(any(Collector.class))).thenReturn(savedCollector);
    when(apiTokenService.generateToken(11L, "FACEBOOK", "fb-123", "seiya@example.com"))
        .thenReturn("jwt-created");
    when(apiTokenService.ttlSeconds()).thenReturn(7200L);

    CollectorLoginResp response =
        service.login("facebook", new CollectorLoginReq(null, "fb-access-token"));

    assertThat(response.collectorId()).isEqualTo(11L);
    assertThat(response.displayName()).isEqualTo("Seiya");
    assertThat(response.email()).isEqualTo("seiya@example.com");
    assertThat(response.accessToken()).isEqualTo("jwt-created");
    assertThat(response.expiresInSeconds()).isEqualTo(7200L);

    ArgumentCaptor<Collector> collectorCaptor = ArgumentCaptor.forClass(Collector.class);
    verify(collectorRepository).save(collectorCaptor.capture());
    assertThat(collectorCaptor.getValue().getId()).isNull();
    assertThat(collectorCaptor.getValue().getDisplayName()).isEqualTo("Seiya");
    assertThat(collectorCaptor.getValue().getEmail()).isEqualTo("seiya@example.com");
    assertThat(collectorCaptor.getValue().getProfilePictureUrl()).isNull();
    assertThat(collectorCaptor.getValue().getRole()).isEqualTo(basicRole);

    ArgumentCaptor<CollectorAuthProvider> providerCaptor =
        ArgumentCaptor.forClass(CollectorAuthProvider.class);
    verify(collectorAuthProviderRepository).save(providerCaptor.capture());

    CollectorAuthProvider persistedLink = providerCaptor.getValue();
    assertThat(persistedLink.getCollector()).isEqualTo(savedCollector);
    assertThat(persistedLink.getProvider()).isEqualTo(ProviderType.FACEBOOK);
    assertThat(persistedLink.getProviderUserId()).isEqualTo("fb-123");
    assertThat(persistedLink.getEmail()).isEqualTo("seiya@example.com");
    assertThat(persistedLink.getEmailVerified()).isTrue();
  }

  @Test
  void loginWithGoogle_shouldThrowIllegalArgumentException_whenIdTokenIsMissing() {
    CollectorLoginReq request = new CollectorLoginReq("   ", null);

    assertThatThrownBy(() -> service.login("google", request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Google idToken is required");
  }

  @Test
  void loginWithGoogle_shouldThrowCollectorInvalidTokenException_whenIssuerIsInvalid() {
    when(googleApiClient.validateIdToken("google-id-token"))
        .thenReturn(
            googleToken(
                "https://evil.example.com",
                "google-client-id",
                "sub-1",
                Instant.now().plusSeconds(600).getEpochSecond()));

    assertThatThrownBy(
            () -> service.login("google", new CollectorLoginReq("google-id-token", null)))
        .isInstanceOf(CollectorInvalidTokenException.class)
        .hasMessage("Google token issuer is invalid");
  }

  @Test
  void loginWithGoogle_shouldThrowCollectorInvalidTokenException_whenAudienceIsInvalid() {
    when(googleCredentials.clientId()).thenReturn("expected-client");
    when(googleApiClient.validateIdToken("google-id-token"))
        .thenReturn(
            googleToken(
                "https://accounts.google.com",
                "another-client",
                "sub-1",
                Instant.now().plusSeconds(600).getEpochSecond()));

    assertThatThrownBy(
            () -> service.login("google", new CollectorLoginReq("google-id-token", null)))
        .isInstanceOf(CollectorInvalidTokenException.class)
        .hasMessage("Google token audience is invalid");
  }

  @Test
  void loginWithGoogle_shouldThrowCollectorInvalidTokenException_whenExpiryClaimIsNotNumeric() {
    when(googleCredentials.clientId()).thenReturn("expected-client");
    when(googleApiClient.validateIdToken("google-id-token"))
        .thenReturn(
            new GoogleTokenInfoResponse(
                "https://accounts.google.com",
                "expected-client",
                "sub-1",
                "shun@example.com",
                "true",
                "Shun",
                "https://img/shun.jpg",
                "not-a-number"));

    assertThatThrownBy(
            () -> service.login("google", new CollectorLoginReq("google-id-token", null)))
        .isInstanceOf(CollectorInvalidTokenException.class)
        .hasMessage("Google token expiry claim is invalid");
  }

  @Test
  void loginWithGoogle_shouldThrowCollectorInvalidTokenException_whenTokenIsExpired() {
    when(googleCredentials.clientId()).thenReturn("expected-client");
    when(googleApiClient.validateIdToken("google-id-token"))
        .thenReturn(
            googleToken(
                "https://accounts.google.com",
                "expected-client",
                "sub-1",
                Instant.now().minusSeconds(5).getEpochSecond()));

    assertThatThrownBy(
            () -> service.login("google", new CollectorLoginReq("google-id-token", null)))
        .isInstanceOf(CollectorInvalidTokenException.class)
        .hasMessage("Google token is expired");
  }

  @Test
  void loginWithGoogle_shouldThrowCollectorInvalidTokenException_whenTokenExpiresNow() {
    when(googleCredentials.clientId()).thenReturn("expected-client");
    when(googleApiClient.validateIdToken("google-id-token"))
        .thenReturn(
            googleToken(
                "https://accounts.google.com",
                "expected-client",
                "sub-1",
                Instant.now().getEpochSecond()));

    assertThatThrownBy(
            () -> service.login("google", new CollectorLoginReq("google-id-token", null)))
        .isInstanceOf(CollectorInvalidTokenException.class)
        .hasMessage("Google token is expired");
  }

  @Test
  void loginWithGoogle_shouldThrowCollectorInvalidTokenException_whenSubjectIsBlank() {
    when(googleCredentials.clientId()).thenReturn("expected-client");
    when(googleApiClient.validateIdToken("google-id-token"))
        .thenReturn(
            googleToken(
                "accounts.google.com",
                "expected-client",
                "   ",
                Instant.now().plusSeconds(500).getEpochSecond()));

    assertThatThrownBy(
            () -> service.login("google", new CollectorLoginReq("google-id-token", null)))
        .isInstanceOf(CollectorInvalidTokenException.class)
        .hasMessage("Google token subject is missing");
  }

  @Test
  void loginWithGoogle_shouldCreateCollectorAndProvider_whenTokenIsValid() {
    when(googleCredentials.clientId()).thenReturn("expected-client");
    when(googleApiClient.validateIdToken("google-id-token"))
        .thenReturn(
            new GoogleTokenInfoResponse(
                "accounts.google.com",
                "expected-client",
                "sub-456",
                "hyoga@example.com",
                "false",
                "Hyoga",
                "https://img/hyoga.jpg",
                String.valueOf(Instant.now().plusSeconds(1200).getEpochSecond())));
    when(collectorAuthProviderRepository.findByProviderAndProviderUserId(
            ProviderType.GOOGLE, "sub-456"))
        .thenReturn(Optional.empty());
    when(collectorRepository.count()).thenReturn(3L);

    Role basicRole = new Role();
    basicRole.setId(2L);
    basicRole.setDescription("Basic Collector");
    when(roleRepository.findByDescription("Basic Collector")).thenReturn(Optional.of(basicRole));

    Collector savedCollector =
        collector(20L, "Hyoga", "hyoga@example.com", "https://img/hyoga.jpg");
    when(collectorRepository.save(any(Collector.class))).thenReturn(savedCollector);
    when(apiTokenService.generateToken(20L, "GOOGLE", "sub-456", "hyoga@example.com"))
        .thenReturn("jwt-google");
    when(apiTokenService.ttlSeconds()).thenReturn(1800L);

    CollectorLoginResp response =
        service.login("google", new CollectorLoginReq("google-id-token", null));

    assertThat(response.collectorId()).isEqualTo(20L);
    assertThat(response.displayName()).isEqualTo("Hyoga");
    assertThat(response.email()).isEqualTo("hyoga@example.com");
    assertThat(response.accessToken()).isEqualTo("jwt-google");
    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.expiresInSeconds()).isEqualTo(1800L);

    ArgumentCaptor<Collector> collectorCaptor = ArgumentCaptor.forClass(Collector.class);
    verify(collectorRepository).save(collectorCaptor.capture());
    assertThat(collectorCaptor.getValue().getDisplayName()).isEqualTo("Hyoga");
    assertThat(collectorCaptor.getValue().getEmail()).isEqualTo("hyoga@example.com");
    assertThat(collectorCaptor.getValue().getProfilePictureUrl())
        .isEqualTo("https://img/hyoga.jpg");
    assertThat(collectorCaptor.getValue().getRole()).isEqualTo(basicRole);

    ArgumentCaptor<CollectorAuthProvider> providerCaptor =
        ArgumentCaptor.forClass(CollectorAuthProvider.class);
    verify(collectorAuthProviderRepository).save(providerCaptor.capture());
    assertThat(providerCaptor.getValue().getProvider()).isEqualTo(ProviderType.GOOGLE);
    assertThat(providerCaptor.getValue().getProviderUserId()).isEqualTo("sub-456");
    assertThat(providerCaptor.getValue().getEmail()).isEqualTo("hyoga@example.com");
    assertThat(providerCaptor.getValue().getEmailVerified()).isFalse();

    verify(apiTokenService).generateToken(20L, "GOOGLE", "sub-456", "hyoga@example.com");
    verify(apiTokenService).ttlSeconds();
  }

  @Test
  void loginWithGoogle_shouldReuseExistingCollector_whenProviderLinkExists() {
    Collector existingCollector =
        collector(33L, "Shiryu", "shiryu@example.com", "https://img/shiryu.jpg");
    CollectorAuthProvider providerLink =
        providerLink(existingCollector, ProviderType.GOOGLE, "sub-999");

    when(googleCredentials.clientId()).thenReturn("expected-client");
    when(googleApiClient.validateIdToken("google-id-token"))
        .thenReturn(
            googleToken(
                "https://accounts.google.com",
                "expected-client",
                "sub-999",
                Instant.now().plusSeconds(900).getEpochSecond()));
    when(collectorAuthProviderRepository.findByProviderAndProviderUserId(
            ProviderType.GOOGLE, "sub-999"))
        .thenReturn(Optional.of(providerLink));
    when(apiTokenService.generateToken(33L, "GOOGLE", "sub-999", "shiryu@example.com"))
        .thenReturn("jwt-existing");
    when(apiTokenService.ttlSeconds()).thenReturn(600L);

    CollectorLoginResp response =
        service.login("google", new CollectorLoginReq("google-id-token", null));

    assertThat(response.collectorId()).isEqualTo(33L);
    assertThat(response.displayName()).isEqualTo("Shiryu");
    assertThat(response.email()).isEqualTo("shiryu@example.com");
    assertThat(response.accessToken()).isEqualTo("jwt-existing");
    assertThat(response.expiresInSeconds()).isEqualTo(600L);

    verify(collectorRepository, never()).save(any(Collector.class));
    verify(collectorAuthProviderRepository, never()).save(any(CollectorAuthProvider.class));
    verify(apiTokenService)
        .generateToken(eq(33L), eq("GOOGLE"), eq("sub-999"), eq("shiryu@example.com"));
  }

  @Test
  void loginWithFacebook_shouldAssignAdminRole_whenFirstCollectorIsCreated() {
    when(fcCredentials.appId()).thenReturn("myth-app-id");
    when(fbApiClient.validateAccessToken("fb-access-token"))
        .thenReturn(new FbTokenResponse(fbTokenData("myth-app-id", true, "fb-100")));
    when(fbApiClient.getUserInfo("fb-access-token"))
        .thenReturn(new FbUserInfoResponse("fb-100", "Mu", "mu@example.com"));
    when(collectorAuthProviderRepository.findByProviderAndProviderUserId(
            ProviderType.FACEBOOK, "fb-100"))
        .thenReturn(Optional.empty());
    when(collectorRepository.count()).thenReturn(0L);

    Collector savedCollector = collector(1L, "Mu", "mu@example.com", null);
    Role adminRole = new Role();
    adminRole.setId(1L);
    adminRole.setDescription("Admin");
    when(roleRepository.findByDescription("Admin")).thenReturn(Optional.of(adminRole));
    when(collectorRepository.save(any(Collector.class))).thenReturn(savedCollector);
    when(apiTokenService.generateToken(1L, "FACEBOOK", "fb-100", "mu@example.com"))
        .thenReturn("jwt-admin");
    when(apiTokenService.ttlSeconds()).thenReturn(3600L);

    CollectorLoginResp response =
        service.login("facebook", new CollectorLoginReq(null, "fb-access-token"));

    assertThat(response.collectorId()).isEqualTo(1L);
    assertThat(response.displayName()).isEqualTo("Mu");

    ArgumentCaptor<Collector> collectorCaptor = ArgumentCaptor.forClass(Collector.class);
    verify(collectorRepository).save(collectorCaptor.capture());
    assertThat(collectorCaptor.getValue().getRole()).isEqualTo(adminRole);
  }

  @Test
  void loginWithFacebook_shouldAssignBasicCollectorRole_whenNonFirstCollectorIsCreated() {
    when(fcCredentials.appId()).thenReturn("myth-app-id");
    when(fbApiClient.validateAccessToken("fb-access-token"))
        .thenReturn(new FbTokenResponse(fbTokenData("myth-app-id", true, "fb-200")));
    when(fbApiClient.getUserInfo("fb-access-token"))
        .thenReturn(new FbUserInfoResponse("fb-200", "Camus", "camus@example.com"));
    when(collectorAuthProviderRepository.findByProviderAndProviderUserId(
            ProviderType.FACEBOOK, "fb-200"))
        .thenReturn(Optional.empty());
    when(collectorRepository.count()).thenReturn(5L);

    Collector savedCollector = collector(6L, "Camus", "camus@example.com", null);
    Role basicRole = new Role();
    basicRole.setId(2L);
    basicRole.setDescription("Basic Collector");
    when(roleRepository.findByDescription("Basic Collector")).thenReturn(Optional.of(basicRole));
    when(collectorRepository.save(any(Collector.class))).thenReturn(savedCollector);
    when(apiTokenService.generateToken(6L, "FACEBOOK", "fb-200", "camus@example.com"))
        .thenReturn("jwt-basic");
    when(apiTokenService.ttlSeconds()).thenReturn(3600L);

    CollectorLoginResp response =
        service.login("facebook", new CollectorLoginReq(null, "fb-access-token"));

    assertThat(response.collectorId()).isEqualTo(6L);

    ArgumentCaptor<Collector> collectorCaptor = ArgumentCaptor.forClass(Collector.class);
    verify(collectorRepository).save(collectorCaptor.capture());
    assertThat(collectorCaptor.getValue().getRole()).isEqualTo(basicRole);
  }

  @Test
  void loginWithGoogle_shouldCreateRoleWhenNotFound_forFirstCollector() {
    when(googleCredentials.clientId()).thenReturn("expected-client");
    when(googleApiClient.validateIdToken("google-id-token"))
        .thenReturn(
            new GoogleTokenInfoResponse(
                "accounts.google.com",
                "expected-client",
                "sub-role-1",
                "aiolia@example.com",
                "true",
                "Aiolia",
                "https://img/aiolia.jpg",
                String.valueOf(Instant.now().plusSeconds(1200).getEpochSecond())));
    when(collectorAuthProviderRepository.findByProviderAndProviderUserId(
            ProviderType.GOOGLE, "sub-role-1"))
        .thenReturn(Optional.empty());
    when(collectorRepository.count()).thenReturn(0L);

    Role adminRole = new Role();
    adminRole.setId(3L);
    adminRole.setDescription("Admin");
    when(roleRepository.findByDescription("Admin")).thenReturn(Optional.empty());
    when(roleRepository.save(any(Role.class))).thenReturn(adminRole);

    Collector savedCollector =
        collector(10L, "Aiolia", "aiolia@example.com", "https://img/aiolia.jpg");
    when(collectorRepository.save(any(Collector.class))).thenReturn(savedCollector);
    when(apiTokenService.generateToken(10L, "GOOGLE", "sub-role-1", "aiolia@example.com"))
        .thenReturn("jwt-new-role");
    when(apiTokenService.ttlSeconds()).thenReturn(1800L);

    CollectorLoginResp response =
        service.login("google", new CollectorLoginReq("google-id-token", null));

    assertThat(response.collectorId()).isEqualTo(10L);

    ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
    verify(roleRepository).save(roleCaptor.capture());
    assertThat(roleCaptor.getValue().getDescription()).isEqualTo("Admin");

    ArgumentCaptor<Collector> collectorCaptor = ArgumentCaptor.forClass(Collector.class);
    verify(collectorRepository).save(collectorCaptor.capture());
    assertThat(collectorCaptor.getValue().getRole()).isEqualTo(adminRole);
  }

  private FbTokenData fbTokenData(String appId, boolean valid, String userId) {
    return new FbTokenData(
        appId, "USER", "myth-cloth", 0L, 0L, valid, new String[] {"email"}, userId);
  }

  private GoogleTokenInfoResponse googleToken(
      String iss, String aud, String sub, long expEpochSecond) {
    return new GoogleTokenInfoResponse(
        iss,
        aud,
        sub,
        "shiryu@example.com",
        "true",
        "Dragon Shiryu",
        "https://img/shiryu.jpg",
        String.valueOf(expEpochSecond));
  }

  private Collector collector(Long id, String displayName, String email, String profilePictureUrl) {
    Collector collector = new Collector();
    collector.setId(id);
    collector.setDisplayName(displayName);
    collector.setEmail(email);
    collector.setProfilePictureUrl(profilePictureUrl);
    return collector;
  }

  private CollectorAuthProvider providerLink(
      Collector collector, ProviderType providerType, String providerUserId) {
    CollectorAuthProvider authProvider = new CollectorAuthProvider();
    authProvider.setCollector(collector);
    authProvider.setProvider(providerType);
    authProvider.setProviderUserId(providerUserId);
    return authProvider;
  }
}
