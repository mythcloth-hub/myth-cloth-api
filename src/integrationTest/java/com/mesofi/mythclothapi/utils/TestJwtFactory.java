package com.mesofi.mythclothapi.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class TestJwtFactory {

  public static String createAdminToken(String secret) throws Exception {

    JWTClaimsSet claims =
        new JWTClaimsSet.Builder()
            .subject("1")
            .claim("name", "Test User")
            .claim("roles", List.of("ADMIN"))
            .claim(
                "permissions",
                List.of("catalogs:read", "catalogs:write", "catalogs:update", "catalogs:delete"))
            .issuer("myth-cloth-api")
            .expirationTime(Date.from(Instant.now().plus(365, ChronoUnit.DAYS)))
            .build();

    SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);

    jwt.sign(new MACSigner(secret.getBytes()));

    return jwt.serialize();
  }
}
