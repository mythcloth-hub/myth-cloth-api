package com.mesofi.mythclothapi.collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mesofi.mythclothapi.BootstrapProperties;
import com.mesofi.mythclothapi.collectorproviders.CollectorAuthProviderRepository;
import com.mesofi.mythclothapi.collectorproviders.model.CollectorAuthProvider;
import com.mesofi.mythclothapi.collectorproviders.model.ProviderType;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginResp;
import com.mesofi.mythclothapi.collectors.dto.CollectorSignupReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorSignupResp;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorEmailAlreadyExistsException;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorInvalidTokenException;
import com.mesofi.mythclothapi.demo.DemoProperties;
import com.mesofi.mythclothapi.integration.fb.FbApiClient;
import com.mesofi.mythclothapi.integration.fb.FbTokenData;
import com.mesofi.mythclothapi.integration.fb.FbTokenError;
import com.mesofi.mythclothapi.integration.fb.FbTokenResponse;
import com.mesofi.mythclothapi.integration.fb.FbUserInfoResponse;
import com.mesofi.mythclothapi.integration.fb.FcCredentialsProperties;
import com.mesofi.mythclothapi.integration.google.GoogleApiClient;
import com.mesofi.mythclothapi.integration.google.GoogleCredentialsProperties;
import com.mesofi.mythclothapi.integration.google.GoogleTokenInfoResponse;
import com.mesofi.mythclothapi.security.roles.RoleRepository;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleNotFoundException;
import com.mesofi.mythclothapi.security.roles.model.Role;
import com.mesofi.mythclothapi.security.service.ApiTokenService;

@ExtendWith(MockitoExtension.class)
class CollectorServiceTest {

    @InjectMocks
    private CollectorService service;

    @Mock
    private CollectorRepository collectorRepository;
    @Mock
    private CollectorAuthProviderRepository collectorAuthProviderRepository;
    @Mock
    private FbApiClient fbApiClient;
    @Mock
    private BootstrapProperties bootstrapProperties;
    @Mock
    private FcCredentialsProperties fcCredentials;
    @Mock
    private GoogleApiClient googleApiClient;
    @Mock
    private GoogleCredentialsProperties googleCredentials;
    @Mock
    private ApiTokenService apiTokenService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private DemoProperties demoProperties;
    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        lenient().when(bootstrapProperties.admin()).thenReturn(Map.of(ProviderType.LOCAL, "demo-admin",
                ProviderType.FACEBOOK, "fb-admin", ProviderType.GOOGLE, "google-admin"));
    }

    @Test
    void login_shouldThrowIllegalArgumentException_whenProviderIsBlank() {
        CollectorLoginReq request = new CollectorLoginReq("id-token", "access-token", null, null);

        assertThatThrownBy(() -> service.login("   ", request)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Provider is required");
    }

    @Test
    void login_shouldThrowIllegalArgumentException_whenProviderIsNull() {
        CollectorLoginReq request = new CollectorLoginReq("id-token", "access-token", null, null);

        assertThatThrownBy(() -> service.login(null, request)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Provider is required");
    }

    @Test
    void login_shouldThrowIllegalArgumentException_whenProviderIsUnknown() {
        CollectorLoginReq request = new CollectorLoginReq("id-token", "access-token", null, null);

        assertThatThrownBy(() -> service.login("twitter", request)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported provider: twitter");
    }

    @Test
    void login_shouldThrowIllegalArgumentException_whenProviderIsNotSupportedYet() {
        CollectorLoginReq request = new CollectorLoginReq("id-token", "access-token", null, null);

        assertThatThrownBy(() -> service.login("github", request)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Provider GITHUB is not supported yet");
    }

    @Test
    void loginWithSelfUser_shouldAuthenticateWithEmailAndPassword() {
        Collector collector = collector(12L, "Mask", "mask@example.com", null);
        collector.setPasswordHash("hashed-password");
        Role role = new Role();
        role.setId(2L);
        role.setName("Collector");
        collector.setRole(role);

        when(collectorRepository.findByEmail("mask@example.com")).thenReturn(Optional.of(collector));
        when(passwordEncoder.matches("Abcdef1!", "hashed-password")).thenReturn(true);
        when(collectorAuthProviderRepository.findByProviderAndProviderUserId(ProviderType.SELF_USER, "myth_12"))
                .thenReturn(Optional.of(providerLink(collector, ProviderType.SELF_USER, "myth_12")));
        when(apiTokenService.generateToken(any(Collector.class), eq("SELF_USER"), eq("myth_12"),
                eq("mask@example.com"))).thenReturn("jwt-self");
        when(apiTokenService.ttlSeconds()).thenReturn(600L);

        CollectorLoginResp response = service.login("self_user",
                new CollectorLoginReq(null, null, "mask@example.com", "Abcdef1!"));

        assertThat(response.collectorId()).isEqualTo(12L);
        assertThat(response.displayName()).isEqualTo("Mask");
        assertThat(response.email()).isEqualTo("mask@example.com");
        assertThat(response.accessToken()).isEqualTo("jwt-self");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(600L);
        verify(passwordEncoder).matches("Abcdef1!", "hashed-password");
    }

    @Test
    void loginWithSelfUser_shouldThrowInvalidCredentials_whenPasswordDoesNotMatch() {
        Collector collector = collector(12L, "Mask", "mask@example.com", null);
        collector.setPasswordHash("hashed-password");

        when(collectorRepository.findByEmail("mask@example.com")).thenReturn(Optional.of(collector));
        when(passwordEncoder.matches("wrong-pass", "hashed-password")).thenReturn(false);

        assertThatThrownBy(
                () -> service.login("self_user", new CollectorLoginReq(null, null, "mask@example.com", "wrong-pass")))
                .isInstanceOf(com.mesofi.mythclothapi.collectors.exceptions.CollectorInvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void loginWithLocal_shouldAssignAdminRole_whenCollectorAlreadyExists() {
        when(demoProperties.providerUserId()).thenReturn("demo-user-1");
        when(demoProperties.name()).thenReturn("Demo Collector");
        when(demoProperties.email()).thenReturn("demo@example.com");
        when(bootstrapProperties.admin()).thenReturn(Map.of(ProviderType.LOCAL, "demo-user-1", ProviderType.FACEBOOK,
                "fb-admin", ProviderType.GOOGLE, "google-admin"));
        when(collectorAuthProviderRepository.findByProviderAndProviderUserId(ProviderType.LOCAL, "demo-user-1"))
                .thenReturn(Optional.empty());

        Role adminRole = new Role();
        adminRole.setId(99L);
        adminRole.setName("Admin");
        when(roleRepository.findByName("Admin")).thenReturn(Optional.of(adminRole));

        when(collectorRepository.save(any(Collector.class))).thenAnswer(invocation -> {
            Collector entity = invocation.getArgument(0);
            entity.setId(55L);
            return entity;
        });
        when(collectorAuthProviderRepository.save(any(CollectorAuthProvider.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(apiTokenService.generateToken(any(Collector.class), eq("LOCAL"), eq("demo-user-1"),
                eq("demo@example.com"))).thenReturn("jwt-local");
        when(apiTokenService.ttlSeconds()).thenReturn(1800L);

        CollectorLoginResp response = service.login("local", new CollectorLoginReq(null, null, null, null));

        assertThat(response.collectorId()).isEqualTo(55L);
        assertThat(response.displayName()).isEqualTo("Demo Collector");
        assertThat(response.email()).isEqualTo("demo@example.com");
        assertThat(response.accessToken()).isEqualTo("jwt-local");
        assertThat(response.expiresInSeconds()).isEqualTo(1800L);

        ArgumentCaptor<Collector> collectorCaptor = ArgumentCaptor.forClass(Collector.class);
        verify(collectorRepository).save(collectorCaptor.capture());
        assertThat(collectorCaptor.getValue().getRole()).isEqualTo(adminRole);

        ArgumentCaptor<CollectorAuthProvider> providerCaptor = ArgumentCaptor.forClass(CollectorAuthProvider.class);
        verify(collectorAuthProviderRepository).save(providerCaptor.capture());
        assertThat(providerCaptor.getValue().getProvider()).isEqualTo(ProviderType.LOCAL);
        assertThat(providerCaptor.getValue().getProviderUserId()).isEqualTo("demo-user-1");
        assertThat(providerCaptor.getValue().getEmail()).isEqualTo("demo@example.com");
        assertThat(providerCaptor.getValue().getEmailVerified()).isTrue();
        assertThat(providerCaptor.getValue().getLastLogin()).isNotNull();
    }

    @Test
    void loginWithLocal_shouldAssignDemoRole_whenUserIsNotConfiguredAdmin() {
        when(demoProperties.providerUserId()).thenReturn("demo-user-2");
        when(demoProperties.name()).thenReturn("Demo Visitor");
        when(demoProperties.email()).thenReturn("visitor@example.com");
        when(bootstrapProperties.admin()).thenReturn(Map.of(ProviderType.LOCAL, "demo-admin", ProviderType.FACEBOOK,
                "fb-admin", ProviderType.GOOGLE, "google-admin"));
        when(collectorAuthProviderRepository.findByProviderAndProviderUserId(ProviderType.LOCAL, "demo-user-2"))
                .thenReturn(Optional.empty());

        Role demoRole = new Role();
        demoRole.setId(42L);
        demoRole.setName("Demo");
        when(roleRepository.findByName("Demo")).thenReturn(Optional.of(demoRole));

        when(collectorRepository.save(any(Collector.class))).thenAnswer(invocation -> {
            Collector entity = invocation.getArgument(0);
            entity.setId(88L);
            return entity;
        });
        when(collectorAuthProviderRepository.save(any(CollectorAuthProvider.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(apiTokenService.generateToken(any(Collector.class), eq("LOCAL"), eq("demo-user-2"),
                eq("visitor@example.com"))).thenReturn("jwt-demo");
        when(apiTokenService.ttlSeconds()).thenReturn(900L);

        CollectorLoginResp response = service.login("local", new CollectorLoginReq(null, null, null, null));

        assertThat(response.collectorId()).isEqualTo(88L);
        assertThat(response.accessToken()).isEqualTo("jwt-demo");
        assertThat(response.expiresInSeconds()).isEqualTo(900L);

        ArgumentCaptor<Collector> collectorCaptor = ArgumentCaptor.forClass(Collector.class);
        verify(collectorRepository).save(collectorCaptor.capture());
        assertThat(collectorCaptor.getValue().getRole()).isEqualTo(demoRole);

        ArgumentCaptor<CollectorAuthProvider> providerCaptor = ArgumentCaptor.forClass(CollectorAuthProvider.class);
        verify(collectorAuthProviderRepository).save(providerCaptor.capture());
        assertThat(providerCaptor.getValue().getLastLogin()).isNotNull();
    }

    @Test
    void loginWithFacebook_shouldThrowIllegalArgumentException_whenAccessTokenIsMissing() {
        CollectorLoginReq request = new CollectorLoginReq(null, "   ", null, null);

        assertThatThrownBy(() -> service.login("facebook", request)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Facebook access token is required");
    }

    @Test
    void loginWithFacebook_shouldThrowCollectorInvalidTokenException_whenTokenIsInvalid() {
        CollectorLoginReq request = new CollectorLoginReq(null, "fb-access-token", null, null);

        when(fcCredentials.appId()).thenReturn("myth-app-id");
        when(fbApiClient.validateAccessToken("fb-access-token"))
                .thenReturn(new FbTokenResponse(fbTokenData("myth-app-id", false, "user-1")));

        assertThatThrownBy(() -> service.login("facebook", request)).isInstanceOf(CollectorInvalidTokenException.class)
                .hasMessage("Facebook token is invalid.");
    }

    @Test
    void loginWithFacebook_shouldThrowCollectorInvalidTokenException_whenTokenIsUnparseable() {
        CollectorLoginReq request = new CollectorLoginReq(null, "fb-access-token", null, null);

        when(fcCredentials.appId()).thenReturn("myth-app-id");
        when(fbApiClient.validateAccessToken("fb-access-token"))
                .thenReturn(new FbTokenResponse(fbTokenDataError(999, "Unable to parse Token")));

        assertThatThrownBy(() -> service.login("facebook", request)).isInstanceOf(CollectorInvalidTokenException.class)
                .hasMessage("Facebook token is invalid. Reason: Unable to parse Token");
    }

    @Test
    void loginWithFacebook_shouldThrowCollectorInvalidTokenException_whenAppIdDoesNotMatch() {
        CollectorLoginReq request = new CollectorLoginReq(null, "fb-access-token", null, null);

        when(fcCredentials.appId()).thenReturn("myth-app-id");
        when(fbApiClient.validateAccessToken("fb-access-token"))
                .thenReturn(new FbTokenResponse(fbTokenData("another-app-id", true, "user-1")));

        assertThatThrownBy(() -> service.login("facebook", request)).isInstanceOf(CollectorInvalidTokenException.class)
                .hasMessage("Facebook token is invalid.");
    }

    @Test
    void loginWithFacebook_shouldReuseExistingCollector_whenProviderLinkExists() {
        Collector existingCollector = collector(7L, "Ikki", "ikki@example.com", null);
        Role collectorRole = new Role();
        collectorRole.setId(2L);
        collectorRole.setName("Collector");
        existingCollector.setRole(collectorRole);
        CollectorAuthProvider providerLink = providerLink(existingCollector, ProviderType.FACEBOOK, "fb-777");

        when(fcCredentials.appId()).thenReturn("myth-app-id");
        when(fbApiClient.validateAccessToken("fb-access-token"))
                .thenReturn(new FbTokenResponse(fbTokenData("myth-app-id", true, "fb-777")));
        when(fbApiClient.getUserInfo("fb-access-token"))
                .thenReturn(new FbUserInfoResponse("fb-777", "Phoenix Ikki", "ikki@example.com"));
        when(collectorAuthProviderRepository.findByProviderAndProviderUserId(ProviderType.FACEBOOK, "fb-777"))
                .thenReturn(Optional.of(providerLink));
        when(apiTokenService.generateToken(eq(existingCollector), eq("FACEBOOK"), eq("fb-777"), eq("ikki@example.com")))
                .thenReturn("api-jwt");
        when(apiTokenService.ttlSeconds()).thenReturn(3600L);

        CollectorLoginResp response = service.login("facebook",
                new CollectorLoginReq(null, "fb-access-token", null, null));

        assertThat(response)
                .extracting(CollectorLoginResp::collectorId, CollectorLoginResp::displayName, CollectorLoginResp::email,
                        CollectorLoginResp::accessToken, CollectorLoginResp::tokenType,
                        CollectorLoginResp::expiresInSeconds)
                .containsExactly(7L, "Ikki", "ikki@example.com", "api-jwt", "Bearer", 3600L);

        verify(collectorRepository, never()).save(any(Collector.class));
        verify(collectorAuthProviderRepository, never()).save(any(CollectorAuthProvider.class));
        assertThat(providerLink.getLastLogin()).isNotNull();
    }

    @Test
    void loginWithFacebook_shouldUpdateLastLoginOnExistingProviderLink() {
        Collector existingCollector = collector(8L, "Hyoga", "hyoga@example.com", null);
        Role collectorRole = new Role();
        collectorRole.setId(2L);
        collectorRole.setName("Collector");
        existingCollector.setRole(collectorRole);
        CollectorAuthProvider providerLink = providerLink(existingCollector, ProviderType.FACEBOOK, "fb-last-login");

        when(fcCredentials.appId()).thenReturn("myth-app-id");
        when(fbApiClient.validateAccessToken("fb-access-token"))
                .thenReturn(new FbTokenResponse(fbTokenData("myth-app-id", true, "fb-last-login")));
        when(fbApiClient.getUserInfo("fb-access-token"))
                .thenReturn(new FbUserInfoResponse("fb-last-login", "Hyoga", "hyoga@example.com"));
        when(collectorAuthProviderRepository.findByProviderAndProviderUserId(ProviderType.FACEBOOK, "fb-last-login"))
                .thenReturn(Optional.of(providerLink));
        when(apiTokenService.generateToken(eq(existingCollector), eq("FACEBOOK"), eq("fb-last-login"),
                eq("hyoga@example.com"))).thenReturn("jwt-last-login");
        when(apiTokenService.ttlSeconds()).thenReturn(3600L);

        CollectorLoginResp response = service.login("facebook",
                new CollectorLoginReq(null, "fb-access-token", null, null));

        assertThat(response.accessToken()).isEqualTo("jwt-last-login");
        assertThat(providerLink.getLastLogin()).isNotNull();
    }

    @Test
    void loginWithFacebook_shouldCreateCollectorAndProvider_whenProviderLinkDoesNotExist() {
        when(fcCredentials.appId()).thenReturn("myth-app-id");
        when(bootstrapProperties.admin()).thenReturn(Map.of(ProviderType.LOCAL, "demo-admin", ProviderType.FACEBOOK,
                "fb-admin", ProviderType.GOOGLE, "google-admin"));
        when(fbApiClient.validateAccessToken("fb-access-token"))
                .thenReturn(new FbTokenResponse(fbTokenData("myth-app-id", true, "fb-123")));
        when(fbApiClient.getUserInfo("fb-access-token"))
                .thenReturn(new FbUserInfoResponse("fb-123", "Seiya", "seiya@example.com"));
        when(collectorAuthProviderRepository.findByProviderAndProviderUserId(ProviderType.FACEBOOK, "fb-123"))
                .thenReturn(Optional.empty());

        Role basicRole = new Role();
        basicRole.setId(2L);
        basicRole.setName("Collector");
        when(roleRepository.findByName("Collector")).thenReturn(Optional.of(basicRole));
        when(collectorRepository.save(any(Collector.class))).thenAnswer(invocation -> {
            Collector entity = invocation.getArgument(0);
            entity.setId(11L);
            return entity;
        });
        when(collectorAuthProviderRepository.save(any(CollectorAuthProvider.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(apiTokenService.generateToken(any(Collector.class), eq("FACEBOOK"), eq("fb-123"), eq("seiya@example.com")))
                .thenReturn("jwt-created");
        when(apiTokenService.ttlSeconds()).thenReturn(7200L);

        CollectorLoginResp response = service.login("facebook",
                new CollectorLoginReq(null, "fb-access-token", null, null));

        assertThat(response.collectorId()).isEqualTo(11L);
        assertThat(response.displayName()).isEqualTo("Seiya");
        assertThat(response.email()).isEqualTo("seiya@example.com");
        assertThat(response.accessToken()).isEqualTo("jwt-created");
        assertThat(response.expiresInSeconds()).isEqualTo(7200L);

        ArgumentCaptor<Collector> collectorCaptor = ArgumentCaptor.forClass(Collector.class);
        verify(collectorRepository).save(collectorCaptor.capture());
        assertThat(collectorCaptor.getValue().getId()).isEqualTo(11L);
        assertThat(collectorCaptor.getValue().getDisplayName()).isEqualTo("Seiya");
        assertThat(collectorCaptor.getValue().getEmail()).isEqualTo("seiya@example.com");
        assertThat(collectorCaptor.getValue().getProfilePictureUrl()).isNull();
        assertThat(collectorCaptor.getValue().getRole()).isEqualTo(basicRole);

        ArgumentCaptor<CollectorAuthProvider> providerCaptor = ArgumentCaptor.forClass(CollectorAuthProvider.class);
        verify(collectorAuthProviderRepository).save(providerCaptor.capture());

        CollectorAuthProvider persistedLink = providerCaptor.getValue();
        assertThat(persistedLink.getCollector()).isEqualTo(collectorCaptor.getValue());
        assertThat(persistedLink.getProvider()).isEqualTo(ProviderType.FACEBOOK);
        assertThat(persistedLink.getProviderUserId()).isEqualTo("fb-123");
        assertThat(persistedLink.getEmail()).isEqualTo("seiya@example.com");
        assertThat(persistedLink.getEmailVerified()).isTrue();
        assertThat(persistedLink.getLastLogin()).isNotNull();
    }

    @Test
    void loginWithGoogle_shouldThrowIllegalArgumentException_whenIdTokenIsMissing() {
        CollectorLoginReq request = new CollectorLoginReq("   ", null, null, null);

        assertThatThrownBy(() -> service.login("google", request)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Google idToken is required");
    }

    @Test
    void loginWithGoogle_shouldThrowCollectorInvalidTokenException_whenIssuerIsInvalid() {
        when(googleApiClient.validateIdToken("google-id-token")).thenReturn(googleToken("https://evil.example.com",
                "google-client-id", "sub-1", Instant.now().plusSeconds(600).getEpochSecond()));

        assertThatThrownBy(() -> service.login("google", new CollectorLoginReq("google-id-token", null, null, null)))
                .isInstanceOf(CollectorInvalidTokenException.class).hasMessage("Google token issuer is invalid");
    }

    @Test
    void loginWithGoogle_shouldThrowCollectorInvalidTokenException_whenAudienceIsInvalid() {
        when(googleCredentials.clientId()).thenReturn("expected-client");
        when(googleApiClient.validateIdToken("google-id-token")).thenReturn(googleToken("https://accounts.google.com",
                "another-client", "sub-1", Instant.now().plusSeconds(600).getEpochSecond()));

        assertThatThrownBy(() -> service.login("google", new CollectorLoginReq("google-id-token", null, null, null)))
                .isInstanceOf(CollectorInvalidTokenException.class).hasMessage("Google token audience is invalid");
    }

    @Test
    void loginWithGoogle_shouldThrowCollectorInvalidTokenException_whenExpiryClaimIsNotNumeric() {
        when(googleCredentials.clientId()).thenReturn("expected-client");
        when(googleApiClient.validateIdToken("google-id-token"))
                .thenReturn(new GoogleTokenInfoResponse("https://accounts.google.com", "expected-client", "sub-1",
                        "shun@example.com", "true", "Shun", "https://img/shun.jpg", "not-a-number"));

        assertThatThrownBy(() -> service.login("google", new CollectorLoginReq("google-id-token", null, null, null)))
                .isInstanceOf(CollectorInvalidTokenException.class).hasMessage("Google token expiry claim is invalid");
    }

    @Test
    void loginWithGoogle_shouldThrowCollectorInvalidTokenException_whenTokenIsExpired() {
        when(googleCredentials.clientId()).thenReturn("expected-client");
        when(googleApiClient.validateIdToken("google-id-token")).thenReturn(googleToken("https://accounts.google.com",
                "expected-client", "sub-1", Instant.now().minusSeconds(5).getEpochSecond()));

        assertThatThrownBy(() -> service.login("google", new CollectorLoginReq("google-id-token", null, null, null)))
                .isInstanceOf(CollectorInvalidTokenException.class).hasMessage("Google token is expired");
    }

    @Test
    void loginWithGoogle_shouldThrowCollectorInvalidTokenException_whenTokenExpiresNow() {
        when(googleCredentials.clientId()).thenReturn("expected-client");
        when(googleApiClient.validateIdToken("google-id-token")).thenReturn(
                googleToken("https://accounts.google.com", "expected-client", "sub-1", Instant.now().getEpochSecond()));

        assertThatThrownBy(() -> service.login("google", new CollectorLoginReq("google-id-token", null, null, null)))
                .isInstanceOf(CollectorInvalidTokenException.class).hasMessage("Google token is expired");
    }

    @Test
    void loginWithGoogle_shouldThrowCollectorInvalidTokenException_whenSubjectIsBlank() {
        when(googleCredentials.clientId()).thenReturn("expected-client");
        when(googleApiClient.validateIdToken("google-id-token")).thenReturn(googleToken("accounts.google.com",
                "expected-client", "   ", Instant.now().plusSeconds(500).getEpochSecond()));

        assertThatThrownBy(() -> service.login("google", new CollectorLoginReq("google-id-token", null, null, null)))
                .isInstanceOf(CollectorInvalidTokenException.class).hasMessage("Google token subject is missing");
    }

    @Test
    void loginWithGoogle_shouldCreateCollectorAndProvider_whenTokenIsValid() {
        when(googleCredentials.clientId()).thenReturn("expected-client");
        when(bootstrapProperties.admin()).thenReturn(Map.of(ProviderType.LOCAL, "demo-admin", ProviderType.FACEBOOK,
                "fb-admin", ProviderType.GOOGLE, "google-admin"));
        when(googleApiClient.validateIdToken("google-id-token")).thenReturn(new GoogleTokenInfoResponse(
                "accounts.google.com", "expected-client", "sub-456", "hyoga@example.com", "false", "Hyoga",
                "https://img/hyoga.jpg", String.valueOf(Instant.now().plusSeconds(1200).getEpochSecond())));
        when(collectorAuthProviderRepository.findByProviderAndProviderUserId(ProviderType.GOOGLE, "sub-456"))
                .thenReturn(Optional.empty());

        Role basicRole = new Role();
        basicRole.setId(2L);
        basicRole.setName("Collector");
        when(roleRepository.findByName("Collector")).thenReturn(Optional.of(basicRole));

        when(collectorRepository.save(any(Collector.class))).thenAnswer(invocation -> {
            Collector entity = invocation.getArgument(0);
            entity.setId(20L);
            return entity;
        });
        when(collectorAuthProviderRepository.save(any(CollectorAuthProvider.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(apiTokenService.generateToken(any(Collector.class), eq("GOOGLE"), eq("sub-456"), eq("hyoga@example.com")))
                .thenReturn("jwt-google");
        when(apiTokenService.ttlSeconds()).thenReturn(1800L);

        CollectorLoginResp response = service.login("google",
                new CollectorLoginReq("google-id-token", null, null, null));

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
        assertThat(collectorCaptor.getValue().getProfilePictureUrl()).isEqualTo("https://img/hyoga.jpg");
        assertThat(collectorCaptor.getValue().getRole()).isEqualTo(basicRole);

        ArgumentCaptor<CollectorAuthProvider> providerCaptor = ArgumentCaptor.forClass(CollectorAuthProvider.class);
        verify(collectorAuthProviderRepository).save(providerCaptor.capture());
        assertThat(providerCaptor.getValue().getProvider()).isEqualTo(ProviderType.GOOGLE);
        assertThat(providerCaptor.getValue().getProviderUserId()).isEqualTo("sub-456");
        assertThat(providerCaptor.getValue().getEmail()).isEqualTo("hyoga@example.com");
        assertThat(providerCaptor.getValue().getEmailVerified()).isFalse();
        assertThat(providerCaptor.getValue().getLastLogin()).isNotNull();

        verify(apiTokenService).generateToken(any(Collector.class), eq("GOOGLE"), eq("sub-456"),
                eq("hyoga@example.com"));
        verify(apiTokenService).ttlSeconds();
    }

    @Test
    void loginWithGoogle_shouldReuseExistingCollector_whenProviderLinkExists() {
        Collector existingCollector = collector(33L, "Shiryu", "shiryu@example.com", "https://img/shiryu.jpg");
        Role collectorRole = new Role();
        collectorRole.setId(2L);
        collectorRole.setName("Collector");
        existingCollector.setRole(collectorRole);
        CollectorAuthProvider providerLink = providerLink(existingCollector, ProviderType.GOOGLE, "sub-999");

        when(googleCredentials.clientId()).thenReturn("expected-client");
        when(googleApiClient.validateIdToken("google-id-token")).thenReturn(googleToken("https://accounts.google.com",
                "expected-client", "sub-999", Instant.now().plusSeconds(900).getEpochSecond()));
        when(collectorAuthProviderRepository.findByProviderAndProviderUserId(ProviderType.GOOGLE, "sub-999"))
                .thenReturn(Optional.of(providerLink));
        when(apiTokenService.generateToken(eq(existingCollector), eq("GOOGLE"), eq("sub-999"),
                eq("shiryu@example.com"))).thenReturn("jwt-existing");
        when(apiTokenService.ttlSeconds()).thenReturn(600L);

        CollectorLoginResp response = service.login("google",
                new CollectorLoginReq("google-id-token", null, null, null));

        assertThat(response.collectorId()).isEqualTo(33L);
        assertThat(response.displayName()).isEqualTo("Shiryu");
        assertThat(response.email()).isEqualTo("shiryu@example.com");
        assertThat(response.accessToken()).isEqualTo("jwt-existing");
        assertThat(response.expiresInSeconds()).isEqualTo(600L);

        verify(collectorRepository, never()).save(any(Collector.class));
        verify(collectorAuthProviderRepository, never()).save(any(CollectorAuthProvider.class));
        verify(apiTokenService).generateToken(eq(existingCollector), eq("GOOGLE"), eq("sub-999"),
                eq("shiryu@example.com"));
    }

    @Test
    void loginWithFacebook_shouldAssignAdminRole_whenFirstCollectorIsCreated() {
        when(fcCredentials.appId()).thenReturn("myth-app-id");
        when(bootstrapProperties.admin()).thenReturn(Map.of(ProviderType.LOCAL, "demo-admin", ProviderType.FACEBOOK,
                "fb-100", ProviderType.GOOGLE, "google-admin"));
        when(fbApiClient.validateAccessToken("fb-access-token"))
                .thenReturn(new FbTokenResponse(fbTokenData("myth-app-id", true, "fb-100")));
        when(fbApiClient.getUserInfo("fb-access-token"))
                .thenReturn(new FbUserInfoResponse("fb-100", "Mu", "mu@example.com"));
        when(collectorAuthProviderRepository.findByProviderAndProviderUserId(ProviderType.FACEBOOK, "fb-100"))
                .thenReturn(Optional.empty());

        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("Admin");
        when(roleRepository.findByName("Admin")).thenReturn(Optional.of(adminRole));
        when(collectorRepository.save(any(Collector.class))).thenAnswer(invocation -> {
            Collector entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });
        when(collectorAuthProviderRepository.save(any(CollectorAuthProvider.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(apiTokenService.generateToken(any(Collector.class), eq("FACEBOOK"), eq("fb-100"), eq("mu@example.com")))
                .thenReturn("jwt-admin");
        when(apiTokenService.ttlSeconds()).thenReturn(3600L);

        CollectorLoginResp response = service.login("facebook",
                new CollectorLoginReq(null, "fb-access-token", null, null));

        assertThat(response.collectorId()).isEqualTo(1L);
        assertThat(response.displayName()).isEqualTo("Mu");

        ArgumentCaptor<Collector> collectorCaptor = ArgumentCaptor.forClass(Collector.class);
        verify(collectorRepository).save(collectorCaptor.capture());
        assertThat(collectorCaptor.getValue().getRole()).isEqualTo(adminRole);
    }

    @Test
    void loginWithFacebook_shouldAssignBasicCollectorRole_whenNonFirstCollectorIsCreated() {
        when(fcCredentials.appId()).thenReturn("myth-app-id");
        when(bootstrapProperties.admin()).thenReturn(Map.of(ProviderType.LOCAL, "demo-admin", ProviderType.FACEBOOK,
                "fb-admin", ProviderType.GOOGLE, "google-admin"));
        when(fbApiClient.validateAccessToken("fb-access-token"))
                .thenReturn(new FbTokenResponse(fbTokenData("myth-app-id", true, "fb-200")));
        when(fbApiClient.getUserInfo("fb-access-token"))
                .thenReturn(new FbUserInfoResponse("fb-200", "Camus", "camus@example.com"));
        when(collectorAuthProviderRepository.findByProviderAndProviderUserId(ProviderType.FACEBOOK, "fb-200"))
                .thenReturn(Optional.empty());

        Role basicRole = new Role();
        basicRole.setId(2L);
        basicRole.setName("Collector");
        when(roleRepository.findByName("Collector")).thenReturn(Optional.of(basicRole));
        when(collectorAuthProviderRepository.save(any(CollectorAuthProvider.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(collectorRepository.save(any(Collector.class))).thenAnswer(invocation -> {
            Collector entity = invocation.getArgument(0);
            entity.setId(6L);
            return entity;
        });
        when(apiTokenService.generateToken(any(Collector.class), eq("FACEBOOK"), eq("fb-200"), eq("camus@example.com")))
                .thenReturn("jwt-basic");
        when(apiTokenService.ttlSeconds()).thenReturn(3600L);

        CollectorLoginResp response = service.login("facebook",
                new CollectorLoginReq(null, "fb-access-token", null, null));

        assertThat(response.collectorId()).isEqualTo(6L);

        ArgumentCaptor<Collector> collectorCaptor = ArgumentCaptor.forClass(Collector.class);
        verify(collectorRepository).save(collectorCaptor.capture());
        assertThat(collectorCaptor.getValue().getRole()).isEqualTo(basicRole);
    }

    @Test
    void loginWithGoogle_shouldThrowRoleNotFoundException_whenRoleDoesNotExist() {
        when(googleCredentials.clientId()).thenReturn("expected-client");
        when(bootstrapProperties.admin()).thenReturn(Map.of(ProviderType.LOCAL, "demo-admin", ProviderType.FACEBOOK,
                "fb-admin", ProviderType.GOOGLE, "sub-role-1"));
        when(googleApiClient.validateIdToken("google-id-token")).thenReturn(new GoogleTokenInfoResponse(
                "accounts.google.com", "expected-client", "sub-role-1", "aiolia@example.com", "true", "Aiolia",
                "https://img/aiolia.jpg", String.valueOf(Instant.now().plusSeconds(1200).getEpochSecond())));
        when(collectorAuthProviderRepository.findByProviderAndProviderUserId(ProviderType.GOOGLE, "sub-role-1"))
                .thenReturn(Optional.empty());
        when(roleRepository.findByName("Admin")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("google", new CollectorLoginReq("google-id-token", null, null, null)))
                .isInstanceOf(RoleNotFoundException.class).hasMessage("Role with name Admin was not found");

        verify(collectorRepository, never()).save(any(Collector.class));
        verify(collectorAuthProviderRepository, never()).save(any(CollectorAuthProvider.class));
    }

    @Test
    void signup_shouldCreateCollectorAndReturnResponse() {
        when(collectorRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Abcdef1!")).thenReturn("hashed-password");

        Role collectorRole = new Role();
        collectorRole.setId(2L);
        collectorRole.setName("Collector");
        when(roleRepository.findByName("Collector")).thenReturn(Optional.of(collectorRole));

        when(collectorRepository.save(any(Collector.class))).thenAnswer(invocation -> {
            Collector entity = invocation.getArgument(0);
            entity.setId(99L);
            return entity;
        });

        CollectorSignupResp response = service
                .signup(new CollectorSignupReq("New User", "new@example.com", "Abcdef1!"));

        assertThat(response.collectorId()).isEqualTo(99L);
        assertThat(response.fullName()).isEqualTo("New User");
        assertThat(response.email()).isEqualTo("new@example.com");

        ArgumentCaptor<Collector> collectorCaptor = ArgumentCaptor.forClass(Collector.class);
        verify(collectorRepository).save(collectorCaptor.capture());
        assertThat(collectorCaptor.getValue().getEmail()).isEqualTo("new@example.com");
        assertThat(collectorCaptor.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(collectorCaptor.getValue().getRole()).isEqualTo(collectorRole);
    }

    @Test
    void signup_shouldThrowCollectorEmailAlreadyExistsException_whenEmailAlreadyExists() {
        Collector existing = collector(5L, "Existing", "existing@example.com", null);
        when(collectorRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.signup(new CollectorSignupReq("Existing", "existing@example.com", "Abcdef1!")))
                .isInstanceOf(CollectorEmailAlreadyExistsException.class)
                .hasMessage("Collector with email existing@example.com already exists");

        verify(passwordEncoder, never()).encode(any());
        verify(collectorRepository, never()).save(any(Collector.class));
    }

    private FbTokenData fbTokenData(String appId, boolean valid, String userId) {
        return new FbTokenData(appId, "USER", "myth-cloth", 0L, 0L, valid, new String[]{"email"}, userId, null);
    }

    private FbTokenData fbTokenDataError(int code, String errorMessage) {
        return new FbTokenData(null, null, null, 0L, 0L, false, new String[]{}, null,
                new FbTokenError(code, errorMessage));
    }

    private GoogleTokenInfoResponse googleToken(String iss, String aud, String sub, long expEpochSecond) {
        return new GoogleTokenInfoResponse(iss, aud, sub, "shiryu@example.com", "true", "Dragon Shiryu",
                "https://img/shiryu.jpg", String.valueOf(expEpochSecond));
    }

    private Collector collector(Long id, String displayName, String email, String profilePictureUrl) {
        Collector collector = new Collector();
        collector.setId(id);
        collector.setDisplayName(displayName);
        collector.setEmail(email);
        collector.setProfilePictureUrl(profilePictureUrl);
        return collector;
    }

    private CollectorAuthProvider providerLink(Collector collector, ProviderType providerType, String providerUserId) {
        CollectorAuthProvider authProvider = new CollectorAuthProvider();
        authProvider.setCollector(collector);
        authProvider.setProvider(providerType);
        authProvider.setProviderUserId(providerUserId);
        return authProvider;
    }
}
