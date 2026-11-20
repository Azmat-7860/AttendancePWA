package com.attendance.auth.dto;

public record AuthResponse(
        String accessToken,
        long expiresIn,
        UserSummary user
) {
}
