package com.attendance.user.dto;

import com.attendance.user.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role must not be null")
        Role role
) {
}
