package com.attendance.auth.dto;

import com.attendance.user.entity.Role;
import java.util.UUID;

public record UserSummary(
        UUID id,
        String email,
        String name,
        Role role,
        UUID organizationId
) {
}
