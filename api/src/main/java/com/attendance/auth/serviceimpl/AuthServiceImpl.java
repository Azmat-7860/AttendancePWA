package com.attendance.auth.serviceimpl;

import com.attendance.auth.dto.AuthResponse;
import com.attendance.auth.dto.AuthResult;
import com.attendance.auth.dto.GoogleUserInfo;
import com.attendance.auth.dto.UserSummary;
import com.attendance.auth.entity.RefreshToken;
import com.attendance.auth.repository.RefreshTokenRepository;
import com.attendance.auth.service.AuthService;
import com.attendance.auth.service.GoogleTokenVerifier;
import com.attendance.auth.service.JwtService;
import com.attendance.tenant.TenantContext;
import com.attendance.user.entity.User;
import com.attendance.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final Duration refreshExpiration;

    public AuthServiceImpl(
            GoogleTokenVerifier googleTokenVerifier,
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            @Value("${app.auth.jwt-refresh-expiration-days:30}") long refreshExpirationDays
    ) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.refreshExpiration = Duration.ofDays(refreshExpirationDays);
    }

    @Override
    @Transactional
    public AuthResult loginWithGoogle(String idToken) {
        GoogleUserInfo googleUser = googleTokenVerifier.verify(idToken);
        log.debug("Verified Google ID token for email claim");

        User user = userRepository.findByEmailIgnoreCaseAndActiveTrue(googleUser.email())
                .orElseThrow(() -> {
                    log.warn("Login attempt for uninvited or deactivated email rejected");
                    return new AccessDeniedException("User email is not invited or has been deactivated");
                });

        TenantContext.setTenantId(user.getOrganizationId());
        try {
            if (user.getGoogleSub() == null) {
                user.setGoogleSub(googleUser.sub());
                userRepository.save(user);
                log.info("Linked Google subject to existing user: {}", user.getId());
            }

            return createSessionTokens(user);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    @Transactional
    public AuthResult rotateRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BadCredentialsException("Refresh token is required");
        }

        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid or unrecognized refresh token"));

        TenantContext.setTenantId(token.getOrganizationId());
        try {
            if (token.isRevoked()) {
                log.warn("Detected reused revoked refresh token for user: {}. Revoking all sessions.", token.getUserId());
                List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedAtIsNull(token.getUserId());
                Instant now = Instant.now();
                activeTokens.forEach(t -> t.setRevokedAt(now));
                refreshTokenRepository.saveAll(activeTokens);
                throw new BadCredentialsException("Revoked refresh token reuse detected. All sessions terminated.");
            }

            if (token.isExpired()) {
                log.warn("Expired refresh token presented for user: {}", token.getUserId());
                throw new BadCredentialsException("Refresh token has expired. Please sign in again.");
            }

            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);

            User user = userRepository.findById(token.getUserId())
                    .filter(User::isActive)
                    .orElseThrow(() -> new AccessDeniedException("User is no longer active"));

            log.debug("Rotated refresh token for user: {}", user.getId());
            return createSessionTokens(user);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            TenantContext.setTenantId(token.getOrganizationId());
            try {
                token.setRevokedAt(Instant.now());
                refreshTokenRepository.save(token);
                log.info("Revoked session for user: {}", token.getUserId());
            } finally {
                TenantContext.clear();
            }
        });
    }

    private AuthResult createSessionTokens(User user) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getOrganizationId(),
                user.getRole().name(),
                user.getEmail()
        );

        String rawRefreshToken = UUID.randomUUID() + "-" + UUID.randomUUID();
        String tokenHash = hashToken(rawRefreshToken);
        Instant expiresAt = Instant.now().plus(refreshExpiration);

        RefreshToken refreshToken = new RefreshToken(user.getId(), tokenHash, expiresAt);
        refreshToken.setOrganizationId(user.getOrganizationId());
        refreshTokenRepository.save(refreshToken);
        log.info("Issued new session tokens for user: {} under org: {}", user.getId(), user.getOrganizationId());

        UserSummary summary = new UserSummary(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getOrganizationId()
        );

        AuthResponse authResponse = new AuthResponse(
                accessToken,
                jwtService.getAccessExpirationSeconds(),
                summary
        );

        return new AuthResult(authResponse, rawRefreshToken);
    }

    public static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
