package com.ai_engineering.agent_service.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Stateless JWT validation for tokens minted by {@code auth_service}.
 *
 * <p>This service deliberately does <b>not</b> load users from a database the
 * way {@code auth_service}'s filter does — it has no user store. It only
 * verifies the HMAC signature (using the shared {@code jwt.secret}) and
 * expiry, then reads the identity claims. The signature check is what makes
 * this safe: a token cannot be forged without the shared secret.
 *
 * <p>Kept on the jjwt 0.11.5 API (`parserBuilder`/`setSigningKey`) to match
 * {@code auth_service}; do not bump to 0.12.x without updating both services.
 */
@Service
public class JwtService {

    private final Key signingKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Verifies signature + expiry and returns the caller identity.
     *
     * @throws io.jsonwebtoken.JwtException if the token is malformed, expired,
     *                                      or its signature does not match.
     */
    public AuthenticatedUser parse(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)   // throws on bad signature / expiry
                .getBody();

        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);
        String username = claims.getSubject();
        return new AuthenticatedUser(userId, username, role);
    }
}
