package com.attendance.auth.serviceimpl;

import com.attendance.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey signingKey;
    private final Duration accessExpiration;

    public JwtServiceImpl(
            @Value("${app.auth.jwt-secret}") String jwtSecret,
            @Value("${app.auth.jwt-access-expiration-minutes:15}") long accessExpirationMinutes
    ) {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpiration = Duration.ofMinutes(accessExpirationMinutes);
    }

    @Override
    public String generateAccessToken(UUID userId, UUID organizationId, String role, String email) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("organizationId", organizationId.toString())
                .claim("role", role)
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long getAccessExpirationSeconds() {
        return accessExpiration.toSeconds();
    }
}
