package com.mesofi.mythclothapi.collectorproviders.model;

/** Supported authentication provider types for collector identities. */
public enum ProviderType {
    /** Google OAuth/OpenID Connect provider. */
    GOOGLE,
    /** Facebook OAuth provider. */
    FACEBOOK,
    /** GitHub OAuth provider. */
    GITHUB,
    /** Apple Sign In provider. */
    APPLE,
    /** Self-user, the user provides their own email and password. */
    SELF_USER,
    /** Local, non-social authentication provider. */
    LOCAL
}
