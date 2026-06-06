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
  /** Local, non-social authentication provider. */
  LOCAL
}
