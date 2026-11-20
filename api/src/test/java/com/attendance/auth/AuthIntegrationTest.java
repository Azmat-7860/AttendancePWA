package com.attendance.auth;

import com.attendance.auth.dto.GoogleLoginRequest;
import com.attendance.auth.dto.GoogleUserInfo;
import com.attendance.auth.entity.RefreshToken;
import com.attendance.auth.repository.RefreshTokenRepository;
import com.attendance.auth.service.GoogleTokenVerifier;
import com.attendance.org.Organization;
import com.attendance.org.OrganizationRepository;
import com.attendance.tenant.TenantContext;
import com.attendance.user.entity.Role;
import com.attendance.user.entity.User;
import com.attendance.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private GoogleTokenVerifier googleTokenVerifier;

    private Organization testOrg;
    private User testUser;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        // Seed an Organization
        testOrg = new Organization();
        testOrg.setName("Metro Builders Corp");
        testOrg = organizationRepository.save(testOrg);

        // Seed an invited Supervisor
        TenantContext.setTenantId(testOrg.getId());
        try {
            testUser = new User("supervisor@metro.example.com", "John Supervisor", Role.SUPERVISOR);
            testUser = userRepository.save(testUser);
        } finally {
            TenantContext.clear();
        }
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Invited user can log in with Google and receive short-lived JWT and rotating refresh token")
    void shouldSuccessfullyLoginInvitedUser() throws Exception {
        when(googleTokenVerifier.verify("valid-google-token"))
                .thenReturn(new GoogleUserInfo("google-sub-999", "supervisor@metro.example.com", "John Supervisor", true));

        GoogleLoginRequest request = new GoogleLoginRequest("valid-google-token");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.email").value("supervisor@metro.example.com"))
                .andExpect(jsonPath("$.user.role").value("SUPERVISOR"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/api/v1/auth"))
                .andReturn();

        // Verify google_sub linked in DB
        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("google-sub-999", updated.getGoogleSub());

        // Verify refresh token stored hashed in DB
        List<RefreshToken> tokens = refreshTokenRepository.findByUserIdAndRevokedAtIsNull(testUser.getId());
        assertEquals(1, tokens.size());
        assertNotNull(tokens.get(0).getTokenHash());
    }

    @Test
    @DisplayName("Uninvited email receives 403 Forbidden")
    void shouldRejectUninvitedEmail() throws Exception {
        when(googleTokenVerifier.verify("uninvited-token"))
                .thenReturn(new GoogleUserInfo("stranger-sub", "stranger@unknown.com", "Stranger", true));

        GoogleLoginRequest request = new GoogleLoginRequest("uninvited-token");

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value("Access Denied"));
    }

    @Test
    @DisplayName("Invalid or expired Google token receives 401 Unauthorized")
    void shouldRejectBadGoogleToken() throws Exception {
        when(googleTokenVerifier.verify("expired-token"))
                .thenThrow(new BadCredentialsException("Invalid Google ID token"));

        GoogleLoginRequest request = new GoogleLoginRequest("expired-token");

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value("Authentication Failed"));
    }

    @Test
    @DisplayName("GET /api/v1/me returns authenticated user details with Bearer JWT")
    void shouldFetchCurrentUserWithValidJwt() throws Exception {
        when(googleTokenVerifier.verify("token-for-me"))
                .thenReturn(new GoogleUserInfo("sub-me", "supervisor@metro.example.com", "John Supervisor", true));

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest("token-for-me"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = json.get("accessToken").asText();

        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.email").value("supervisor@metro.example.com"))
                .andExpect(jsonPath("$.role").value("SUPERVISOR"))
                .andExpect(jsonPath("$.organizationId").value(testOrg.getId().toString()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh rotates the refresh token and revoking on reuse works")
    void shouldRotateRefreshTokenAndRevokeOnReuse() throws Exception {
        // 1. Initial login
        when(googleTokenVerifier.verify("token-1"))
                .thenReturn(new GoogleUserInfo("sub-1", "supervisor@metro.example.com", "John Supervisor", true));

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest("token-1"))))
                .andExpect(status().isOk())
                .andReturn();

        Cookie originalCookie = loginResult.getResponse().getCookie("refreshToken");
        assertNotNull(originalCookie);

        // 2. Rotate refresh token
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(originalCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        Cookie rotatedCookie = refreshResult.getResponse().getCookie("refreshToken");
        assertNotNull(rotatedCookie);
        assertNotEquals(originalCookie.getValue(), rotatedCookie.getValue(), "Refresh token must rotate to a new value");

        // 3. Attempting to reuse the original (now revoked) refresh token must be rejected and revoke all sessions!
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(originalCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Revoked refresh token reuse detected. All sessions terminated."));

        // 4. Verify the rotated token was also terminated because reuse was detected
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedAtIsNull(testUser.getId());
        assertTrue(activeTokens.isEmpty(), "All refresh tokens for this user must be revoked upon reuse detection");
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout revokes session and clears cookie")
    void shouldLogoutSuccessfully() throws Exception {
        when(googleTokenVerifier.verify("logout-token"))
                .thenReturn(new GoogleUserInfo("sub-logout", "supervisor@metro.example.com", "John Supervisor", true));

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest("logout-token"))))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = loginResult.getResponse().getCookie("refreshToken");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(cookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refreshToken", 0));

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedAtIsNull(testUser.getId());
        assertTrue(activeTokens.isEmpty(), "Active refresh tokens must be revoked on logout");
    }
}
