package com.mesofi.mythclothapi.collectors;

import static com.mesofi.mythclothapi.collectorproviders.model.ProviderType.FACEBOOK;
import static com.mesofi.mythclothapi.collectorproviders.model.ProviderType.GOOGLE;
import static com.mesofi.mythclothapi.collectorproviders.model.ProviderType.LOCAL;
import static com.mesofi.mythclothapi.collectorproviders.model.ProviderType.SELF_USER;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.mesofi.mythclothapi.BootstrapProperties;
import com.mesofi.mythclothapi.collectorproviders.CollectorAuthProviderRepository;
import com.mesofi.mythclothapi.collectorproviders.model.CollectorAuthProvider;
import com.mesofi.mythclothapi.collectorproviders.model.ProviderType;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorLoginResp;
import com.mesofi.mythclothapi.collectors.dto.CollectorSignupReq;
import com.mesofi.mythclothapi.collectors.dto.CollectorSignupResp;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorEmailAlreadyExistsException;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorEmailNotFoundException;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorInvalidCredentialsException;
import com.mesofi.mythclothapi.collectors.exceptions.CollectorInvalidTokenException;
import com.mesofi.mythclothapi.demo.DemoProperties;
import com.mesofi.mythclothapi.integration.fb.FbApiClient;
import com.mesofi.mythclothapi.integration.fb.FbTokenData;
import com.mesofi.mythclothapi.integration.fb.FbUserInfoResponse;
import com.mesofi.mythclothapi.integration.fb.FcCredentialsProperties;
import com.mesofi.mythclothapi.integration.google.GoogleApiClient;
import com.mesofi.mythclothapi.integration.google.GoogleCredentialsProperties;
import com.mesofi.mythclothapi.integration.google.GoogleTokenInfoResponse;
import com.mesofi.mythclothapi.security.roles.RoleRepository;
import com.mesofi.mythclothapi.security.roles.exceptions.RoleNotFoundException;
import com.mesofi.mythclothapi.security.roles.model.Role;
import com.mesofi.mythclothapi.security.roles.model.RoleType;
import com.mesofi.mythclothapi.security.service.ApiTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service that orchestrates collector social authentication and login.
 *
 * <p>
 * Validates provider tokens, creates or reuses collector/provider associations,
 * and returns the API authentication payload used by the client.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class CollectorService {

    private final CollectorRepository collectorRepository;
    private final CollectorAuthProviderRepository collectorAuthProviderRepository;
    private final PasswordEncoder passwordEncoder;
    private final FbApiClient fbApiClient;
    private final BootstrapProperties bootstrapProperties;
    private final FcCredentialsProperties fcCredentials;
    private final GoogleApiClient googleApiClient;
    private final GoogleCredentialsProperties googleCredentials;
    private final ApiTokenService apiTokenService;
    private final RoleRepository roleRepository;
    private final DemoProperties demoProperties;

    private static final String PREFIX = "myth_";

    /**
     * Logs in a collector using the requested social provider.
     *
     * @param provider
     *            provider name from the API path (for example, facebook or google)
     * @param loginRequest
     *            social login payload with provider token values
     * @return authenticated collector response containing API token details
     * @throws IllegalArgumentException
     *             if provider is missing or unsupported
     */
    @Transactional
    public CollectorLoginResp login(String provider, CollectorLoginReq loginRequest) {
        log.info("User is trying to authenticate with provider '{}'", provider);

        ProviderType providerType = resolveProvider(provider);

        return switch (providerType) {
            case FACEBOOK -> loginWithFacebook(loginRequest.accessToken());
            case GOOGLE -> loginWithGoogle(loginRequest.idToken());
            case SELF_USER -> loginWithEmailAndPassword(loginRequest.email(), loginRequest.password());
            case LOCAL -> loginWithLocal();
            default -> throw new IllegalArgumentException("Provider %s is not supported yet".formatted(providerType));
        };
    }

    /**
     * Registers a new collector account using the provided signup request.
     *
     * @param signupRequest
     *            the collector signup request containing necessary information
     * @return the collector signup response with registered collector details
     * @throws IllegalArgumentException
     *             if the email is already registered
     */
    @Transactional
    public CollectorSignupResp signup(CollectorSignupReq signupRequest) {
        log.info("User is trying to sign up with email '{}'", signupRequest.email());

        collectorRepository.findByEmail(signupRequest.email()).ifPresent(account -> {
            throw new CollectorEmailAlreadyExistsException(account.getEmail());
        });

        // Encodes the password into an Argon2id string format
        String rawPassword = signupRequest.password();
        String hashedPassword = passwordEncoder.encode(rawPassword);

        Role currRole = retrieveRole(RoleType.COLLECTOR, null, null);
        Collector saved = createCollectorAccount(signupRequest.email(), hashedPassword, signupRequest.fullName(), null,
                currRole);

        return new CollectorSignupResp(saved.getId(), saved.getDisplayName(), saved.getEmail());
    }

    /**
     * Validates a Facebook access token and logs in or provisions the related
     * collector.
     *
     * @param accessToken
     *            Facebook user access token
     * @return authenticated collector response containing API token details
     * @throws IllegalArgumentException
     *             if the token is blank
     * @throws CollectorInvalidTokenException
     *             if the token is invalid for this application
     */
    private CollectorLoginResp loginWithFacebook(String accessToken) {
        requireToken(accessToken, "Facebook access token is required");

        FbTokenData fbTokenData = fbApiClient.validateAccessToken(accessToken).data();
        boolean appMatches = fcCredentials.appId().equals(fbTokenData.appId());

        if (!fbTokenData.valid() || !appMatches) {
            String errorMessage = "Facebook token is invalid.";
            if (!fbTokenData.valid() && fbTokenData.error() != null) {
                errorMessage += " Reason: " + fbTokenData.error().message();
            }
            log.warn(errorMessage);
            throw new CollectorInvalidTokenException(errorMessage);
        }

        FbUserInfoResponse userInfo = fbApiClient.getUserInfo(accessToken);
        String providerUserId = userInfo.id();
        String name = userInfo.name();
        String providerEmail = userInfo.email();

        Collector collector = createOrUpdateRegisteredCollector(FACEBOOK, providerUserId, name, providerEmail, true,
                null);

        return buildLoginResponse(collector, FACEBOOK, providerUserId);
    }

    /**
     * Validates a Google ID token and logs in or provisions the related collector.
     *
     * @param idToken
     *            Google ID token
     * @return authenticated collector response containing API token details
     * @throws IllegalArgumentException
     *             if the token is blank
     * @throws CollectorInvalidTokenException
     *             if token claims are invalid or expired
     */
    private CollectorLoginResp loginWithGoogle(String idToken) {
        requireToken(idToken, "Google idToken is required");

        GoogleTokenInfoResponse tokenInfo = googleApiClient.validateIdToken(idToken);
        validateGoogleToken(tokenInfo);

        Collector collector = createOrUpdateRegisteredCollector(GOOGLE, tokenInfo.sub(), tokenInfo.name(),
                tokenInfo.email(), tokenInfo.emailVerified(), tokenInfo.picture());

        return buildLoginResponse(collector, GOOGLE, tokenInfo.sub());
    }

    /**
     * Verifies Google token claims required by this API.
     *
     * @param tokenInfo
     *            parsed token info returned by Google token introspection
     * @throws CollectorInvalidTokenException
     *             if issuer, audience, expiry, or subject is invalid
     */
    private void validateGoogleToken(GoogleTokenInfoResponse tokenInfo) {
        boolean issuerValid = "https://accounts.google.com".equals(tokenInfo.iss())
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
     * Logs in a collector using email and password authentication.
     *
     * @param email
     *            collector's email address
     * @param password
     *            collector's password
     * @return login response payload for API clients
     */
    private CollectorLoginResp loginWithEmailAndPassword(String email, String password) {
        Collector collector = collectorRepository.findByEmail(email).orElseThrow(CollectorEmailNotFoundException::new);

        if (!passwordEncoder.matches(password, collector.getPasswordHash())) {
            throw new CollectorInvalidCredentialsException();
        }

        return toLoginResponse(collector);
    }

    /**
     * Converts an existing collector entity into a login response payload.
     *
     * @param existingCollector
     *            the existing collector entity
     * @return the login response payload for API clients
     */
    private CollectorLoginResp toLoginResponse(Collector existingCollector) {
        String userId = PREFIX + existingCollector.getId();

        Collector collector = createOrUpdateRegisteredCollector(SELF_USER, userId, existingCollector.getDisplayName(),
                existingCollector.getEmail(), false, null);

        return buildLoginResponse(collector, SELF_USER, userId);
    }

    /**
     * Logs in a collector using local demo credentials.
     *
     * @return login response payload for API clients
     */
    private CollectorLoginResp loginWithLocal() {
        String userId = demoProperties.providerUserId();
        String name = demoProperties.name();
        String email = demoProperties.email();

        Collector collector = createOrUpdateRegisteredCollector(LOCAL, userId, name, email, true, null);

        return buildLoginResponse(collector, LOCAL, userId);
    }

    /**
     * Builds the API login response and signs an internal API token for the
     * collector.
     *
     * @param collector
     *            authenticated collector entity
     * @param provider
     *            social provider used for the login
     * @param providerUserId
     *            provider-specific user id
     * @return login response payload for API clients
     */
    private CollectorLoginResp buildLoginResponse(Collector collector, ProviderType provider, String providerUserId) {
        String apiJwt = apiTokenService.generateToken(collector, provider.name(), providerUserId, collector.getEmail());

        return new CollectorLoginResp(collector.getId(), collector.getDisplayName(), collector.getEmail(),
                collector.getRole().getName(), apiJwt, "Bearer", apiTokenService.ttlSeconds());
    }

    /**
     * Resolves an incoming provider string to a supported {@link ProviderType}.
     *
     * @param provider
     *            raw provider value from the request path
     * @return normalized provider enum value
     * @throws IllegalArgumentException
     *             if provider is missing or unsupported
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
     * @param token
     *            token value to validate
     * @param message
     *            error message used when token is missing
     * @throws IllegalArgumentException
     *             if token is null or blank
     */
    private void requireToken(String token, String message) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Creates or retrieves a collector associated with an external authentication
     * provider.
     *
     * <p>
     * If a collector is already registered for the given provider and user ID, the
     * existing collector is returned. Otherwise, a new collector is created and
     * assigned a role based on the provider and configured administrator accounts,
     * and the authentication provider association is persisted.
     * </p>
     *
     * @param providerType
     *            the authentication provider used by the collector
     * @param userId
     *            the unique user ID assigned by the authentication provider
     * @param name
     *            the collector's display name
     * @param email
     *            the collector's email address
     * @param emailVerified
     *            whether the email address has been verified by the provider
     * @param picture
     *            the collector's profile picture URL
     * @return the existing or newly created collector
     * @throws RoleNotFoundException
     *             if the role assigned to the collector does not exist
     */
    private Collector createOrUpdateRegisteredCollector(ProviderType providerType, String userId, String name,
            String email, boolean emailVerified, String picture) {
        log.info("Processing collector authentication for provider: {}, userId: {}", providerType, userId);

        CollectorAuthProvider collectorAuthProvider = collectorAuthProviderRepository
                .findByProviderAndProviderUserId(providerType, userId).orElseGet(() -> {
                    Collector collector = collectorRepository.findByEmail(email).orElseGet(() -> {

                        Role currRole = retrieveRole(null, providerType, userId);
                        return createCollectorAccount(email, null, name, picture, currRole);
                    });

                    CollectorAuthProvider newProvider = new CollectorAuthProvider();
                    newProvider.setProvider(providerType);
                    newProvider.setProviderUserId(userId);
                    newProvider.setEmail(collector.getEmail());
                    newProvider.setEmailVerified(emailVerified);
                    newProvider.setLastLogin(Instant.now());
                    newProvider.setCollector(collector);

                    return collectorAuthProviderRepository.save(newProvider);
                });

        // Update the collector's last login timestamp to the current time, regardless
        // of whether it was newly created or retrieved
        collectorAuthProvider.setLastLogin(Instant.now());
        return collectorAuthProvider.getCollector();
    }

    /**
     * Creates a new collector account with the specified details.
     *
     * @param email
     *            the collector's email address
     * @param hashedPassword
     *            the hashed password for the collector
     * @param displayName
     *            the collector's display name
     * @param profilePictureUrl
     *            the URL of the collector's profile picture
     * @param role
     *            the role assigned to the collector
     * @return the newly created collector
     */
    private Collector createCollectorAccount(String email, String hashedPassword, String displayName,
            String profilePictureUrl, Role role) {
        log.info("Creating new collector with email '{}', display name '{}', and role '{}'", email, displayName,
                role.getName());

        Collector collector = new Collector();
        collector.setEmail(email);
        collector.setPasswordHash(hashedPassword);
        collector.setDisplayName(displayName);
        collector.setProfilePictureUrl(profilePictureUrl);
        collector.setRole(role);

        return collectorRepository.save(collector);
    }

    /**
     * Retrieves a role by name, or determines the appropriate role based on the
     * provider and user ID if the role name is null.
     *
     * @param roleName
     *            the name of the role
     * @param providerType
     *            the type of the provider
     * @param providerUserId
     *            the user ID of the provider
     * @return the retrieved or determined role
     */
    private Role retrieveRole(RoleType roleName, ProviderType providerType, String providerUserId) {

        if (roleName == null) {
            Map<ProviderType, String> adminMap = bootstrapProperties.admin();
            boolean isAdmin = adminMap.getOrDefault(providerType, "").equals(providerUserId);

            roleName = isAdmin ? RoleType.ADMIN : providerType == LOCAL ? RoleType.DEMO : RoleType.COLLECTOR;
        }
        String roleDisplayName = roleName.getDisplayName();
        return roleRepository.findByName(roleDisplayName).orElseThrow(() -> new RoleNotFoundException(roleDisplayName));
    }
}
