package com.attendance.auth.service;

import io.jsonwebtoken.Claims;
import java.util.UUID;

public interface JwtService {

    String generateAccessToken(UUID userId, UUID organizationId, String role, String email);

    Claims extractClaims(String token);

    boolean isTokenValid(String token);

    long getAccessExpirationSeconds();
}
