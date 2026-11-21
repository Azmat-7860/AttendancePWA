package com.attendance.user.dto;

import com.attendance.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteUserRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email format")
        String email,

        @NotBlank(message = "Name must not be blank")
        String name,

        @NotNull(message = "Role must be specified (OWNER or SUPERVISOR)")
        Role role
) {
}
