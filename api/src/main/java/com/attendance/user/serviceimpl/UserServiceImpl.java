package com.attendance.user.serviceimpl;

import com.attendance.auth.entity.RefreshToken;
import com.attendance.auth.repository.RefreshTokenRepository;
import com.attendance.tenant.TenantContext;
import com.attendance.user.dto.InviteUserRequest;
import com.attendance.user.dto.UserResponse;
import com.attendance.user.entity.Role;
import com.attendance.user.entity.User;
import com.attendance.user.repository.UserRepository;
import com.attendance.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserServiceImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public UserResponse inviteUser(InviteUserRequest request) {
        UUID organizationId = TenantContext.requireTenantId();

        if (userRepository.existsByOrganizationIdAndEmailIgnoreCase(organizationId, request.email())) {
            throw new IllegalArgumentException("User with this email is already registered in this organization");
        }

        User user = new User(request.email().toLowerCase().trim(), request.name().trim(), request.role());
        user.setOrganizationId(organizationId);
        user = userRepository.save(user);

        log.info("Invited new user id: {} to organization: {}", user.getId(), organizationId);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'SUPERVISOR')")
    public List<UserResponse> listUsers() {
        UUID organizationId = TenantContext.requireTenantId();
        List<User> users = userRepository.findByOrganizationId(organizationId);

        return users.stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public UserResponse updateUserRole(UUID userId, Role newRole) {
        UUID organizationId = TenantContext.requireTenantId();

        User user = userRepository.findByIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("User not found in organization"));

        user.setRole(newRole);
        user = userRepository.save(user);

        // Terminate existing sessions so user is forced to refresh and obtain a JWT with the new role
        revokeUserSessions(userId);

        log.info("Updated role for user id: {} to {}", userId, newRole);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public UserResponse deactivateUser(UUID userId) {
        UUID organizationId = TenantContext.requireTenantId();

        User user = userRepository.findByIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("User not found in organization"));

        user.setActive(false);
        user = userRepository.save(user);

        // Terminate all sessions immediately
        revokeUserSessions(userId);

        log.info("Deactivated user id: {} and revoked active sessions", userId);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public UserResponse reactivateUser(UUID userId) {
        UUID organizationId = TenantContext.requireTenantId();

        User user = userRepository.findByIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("User not found in organization"));

        user.setActive(true);
        user = userRepository.save(user);

        log.info("Reactivated user id: {}", userId);
        return UserResponse.fromEntity(user);
    }

    private void revokeUserSessions(UUID userId) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId);
        if (!activeTokens.isEmpty()) {
            Instant now = Instant.now();
            activeTokens.forEach(t -> t.setRevokedAt(now));
            refreshTokenRepository.saveAll(activeTokens);
            log.debug("Revoked {} active refresh token(s) for user id: {}", activeTokens.size(), userId);
        }
    }
}
