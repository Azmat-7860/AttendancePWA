package com.attendance.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "idToken must not be blank")
        String idToken
) {
}
