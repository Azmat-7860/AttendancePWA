package com.attendance.user.dto;

import com.attendance.user.entity.Role;
import com.attendance.user.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name,
        Role role,
        boolean active,
        UUID organizationId,
        Instant createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isActive(),
                user.getOrganizationId(),
                user.getCreatedAt()
        );
    }
}
