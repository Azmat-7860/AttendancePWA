package com.attendance.auth.controller;

import com.attendance.auth.dto.AuthResponse;
import com.attendance.auth.dto.AuthResult;
import com.attendance.auth.dto.GoogleLoginRequest;
import com.attendance.auth.dto.UserSummary;
import com.attendance.auth.security.UserPrincipal;
import com.attendance.auth.service.AuthService;
import com.attendance.user.entity.User;
import com.attendance.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final boolean isSecureCookie;

    public AuthController(
            AuthService authService,
            UserRepository userRepository,
            @Value("${app.auth.cookie-secure:false}") boolean isSecureCookie
    ) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.isSecureCookie = isSecureCookie;
    }

    @PostMapping("/auth/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResult result = authService.loginWithGoogle(request.idToken());
        ResponseCookie cookie = createRefreshTokenCookie(result.rawRefreshToken(), Duration.ofDays(30));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.response());
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        AuthResult result = authService.rotateRefreshToken(refreshToken);
        ResponseCookie cookie = createRefreshTokenCookie(result.rawRefreshToken(), Duration.ofDays(30));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.response());
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        authService.logout(refreshToken);
        ResponseCookie clearingCookie = createRefreshTokenCookie("", Duration.ZERO);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearingCookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummary> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new AccessDeniedException("User account not found"));

        UserSummary summary = new UserSummary(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getOrganizationId()
        );

        return ResponseEntity.ok(summary);
    }

    private ResponseCookie createRefreshTokenCookie(String token, Duration maxAge) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(isSecureCookie)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(maxAge)
                .build();
    }
}
