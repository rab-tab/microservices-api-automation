package com.microservices.api.core;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

/**
 * Helper class to generate test JWTs for OAuth negative test scenarios.
 * Does NOT call Auth0. Use for expired tokens, missing scopes, or malformed tokens.
 */
public class JwtTokenProvider {

    // Secret key for testing (must match Spring test profile signing key)
    // HS256 requires at least 256-bit key
    private static final Key TEST_KEY = Keys.hmacShaKeyFor("my-super-secret-test-key-1234567890123456".getBytes());
    // Default validity: 1 hour
    private static final long VALIDITY_MS = 3600_000;

    /**
     * Generates an expired JWT token.
     * @param scopes optional scopes to include in the token
     * @return expired JWT string
     */
    /** Expired token */
    public static String generateExpiredToken(String... scopes) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject("test-user")
                .issuedAt(Date.from(now.minusMillis(2 * VALIDITY_MS)))
                .expiration(Date.from(now.minusSeconds(1)))
                .claim("scope", String.join(" ", scopes))
                .signWith(TEST_KEY) // ✅ NOT deprecated
                .compact();
    }


    /**
     * Generates a JWT token missing required scopes.
     * @param missingScopes description of missing scopes for clarity
     * @return JWT token string
     */
    /** Token missing required scopes */
    public static String generateTokenMissingScope() {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject("test-user")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(VALIDITY_MS)))
                .claim("scope", "other:scope")
                .signWith(TEST_KEY) // ✅ NOT deprecated
                .compact();
    }

    /** Malformed token */
    public static String generateMalformedToken() {
        return "malformed.token.value";
    }
}
