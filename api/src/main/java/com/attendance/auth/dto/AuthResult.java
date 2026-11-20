package com.attendance.auth.dto;

public record AuthResult(
        AuthResponse response,
        String rawRefreshToken
) {
}
