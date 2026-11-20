package com.attendance.auth.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

public record UserPrincipal(
        UUID userId,
        UUID organizationId,
        String email,
        String role
) implements Principal, Serializable {

    @Override
    public String getName() {
        return email;
    }
}
