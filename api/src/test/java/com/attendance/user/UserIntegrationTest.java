package com.attendance.user;

import com.attendance.auth.entity.RefreshToken;
import com.attendance.auth.repository.RefreshTokenRepository;
import com.attendance.auth.service.JwtService;
import com.attendance.org.Organization;
import com.attendance.org.OrganizationRepository;
import com.attendance.tenant.TenantContext;
import com.attendance.user.dto.InviteUserRequest;
import com.attendance.user.dto.UpdateUserRoleRequest;
import com.attendance.user.entity.Role;
import com.attendance.user.entity.User;
import com.attendance.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class UserIntegrationTest {

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

    @Autowired
    private JwtService jwtService;

    private Organization orgA;
    private Organization orgB;
    private User ownerA;
    private User supervisorA;
    private String ownerTokenA;
    private String supervisorTokenA;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        // 1. Setup Organization A
        orgA = new Organization();
        orgA.setName("Acme Builders");
        orgA = organizationRepository.save(orgA);

        TenantContext.setTenantId(orgA.getId());
        try {
            ownerA = new User("owner@acme.example.com", "Alice Owner", Role.OWNER);
            ownerA = userRepository.save(ownerA);

            supervisorA = new User("supervisor@acme.example.com", "Bob Supervisor", Role.SUPERVISOR);
            supervisorA = userRepository.save(supervisorA);
        } finally {
            TenantContext.clear();
        }

        // Generate JWTs
        ownerTokenA = jwtService.generateAccessToken(ownerA.getId(), orgA.getId(), "OWNER", ownerA.getEmail());
        supervisorTokenA = jwtService.generateAccessToken(supervisorA.getId(), orgA.getId(), "SUPERVISOR", supervisorA.getEmail());

        // 2. Setup Organization B
        orgB = new Organization();
        orgB.setName("Beta Construction");
        orgB = organizationRepository.save(orgB);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Owner can invite a supervisor to their organization")
    void ownerCanInviteSupervisor() throws Exception {
        InviteUserRequest request = new InviteUserRequest("charlie@acme.example.com", "Charlie Tech", Role.SUPERVISOR);

        mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + ownerTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("charlie@acme.example.com"))
                .andExpect(jsonPath("$.role").value("SUPERVISOR"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.organizationId").value(orgA.getId().toString()));

        // Verify user persisted
        assertTrue(userRepository.findByEmailIgnoreCaseAndActiveTrue("charlie@acme.example.com").isPresent());
    }

    @Test
    @DisplayName("Supervisor is blocked from inviting users with 403 Forbidden")
    void supervisorCannotInviteUser() throws Exception {
        InviteUserRequest request = new InviteUserRequest("dave@acme.example.com", "Dave Imposter", Role.SUPERVISOR);

        mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + supervisorTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        // Verify user was NOT persisted
        assertFalse(userRepository.findByEmailIgnoreCaseAndActiveTrue("dave@acme.example.com").isPresent());
    }

    @Test
    @DisplayName("Both Owner and Supervisor can list users in their organization")
    void bothRolesCanListUsers() throws Exception {
        // Owner lists users
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + ownerTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Supervisor lists users
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + supervisorTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Owner can change a user's role; Supervisor cannot")
    void testChangeUserRoleAuthorization() throws Exception {
        UpdateUserRoleRequest promoteRequest = new UpdateUserRoleRequest(Role.OWNER);

        // Supervisor tries to promote themselves -> 403 Forbidden
        mockMvc.perform(put("/api/v1/users/" + supervisorA.getId() + "/role")
                        .header("Authorization", "Bearer " + supervisorTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(promoteRequest)))
                .andExpect(status().isForbidden());

        // Owner promotes Supervisor -> 200 OK
        mockMvc.perform(put("/api/v1/users/" + supervisorA.getId() + "/role")
                        .header("Authorization", "Bearer " + ownerTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(promoteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("OWNER"));

        // Verify updated role
        User updated = userRepository.findById(supervisorA.getId()).orElseThrow();
        assertEquals(Role.OWNER, updated.getRole());
    }

    @Test
    @DisplayName("Owner can deactivate a user and all sessions are immediately revoked")
    void ownerCanDeactivateUserAndRevokeSessions() throws Exception {
        // Seed active refresh token for supervisorA
        TenantContext.setTenantId(orgA.getId());
        try {
            RefreshToken token = new RefreshToken(supervisorA.getId(), "test-hash-123", Instant.now().plusSeconds(3600));
            refreshTokenRepository.save(token);
        } finally {
            TenantContext.clear();
        }

        // Supervisor cannot deactivate Owner
        mockMvc.perform(delete("/api/v1/users/" + ownerA.getId())
                        .header("Authorization", "Bearer " + supervisorTokenA))
                .andExpect(status().isForbidden());

        // Owner deactivates Supervisor -> 200 OK
        mockMvc.perform(delete("/api/v1/users/" + supervisorA.getId())
                        .header("Authorization", "Bearer " + ownerTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Verify user is inactive in DB
        User updated = userRepository.findById(supervisorA.getId()).orElseThrow();
        assertFalse(updated.isActive());

        // Verify all refresh tokens for supervisorA are revoked immediately
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedAtIsNull(supervisorA.getId());
        assertTrue(activeTokens.isEmpty(), "Active refresh tokens must be immediately revoked upon user deactivation");
    }

    @Test
    @DisplayName("Cross-tenant isolation: Owner in Org B cannot see or alter users in Org A")
    void crossTenantUserIsolation() throws Exception {
        // Create an Owner in Org B
        User ownerB;
        TenantContext.setTenantId(orgB.getId());
        try {
            ownerB = new User("owner@beta.example.com", "Bob Beta", Role.OWNER);
            ownerB = userRepository.save(ownerB);
        } finally {
            TenantContext.clear();
        }
        String ownerTokenB = jwtService.generateAccessToken(ownerB.getId(), orgB.getId(), "OWNER", ownerB.getEmail());

        // Owner B lists users: should only see Org B's users (1), never Org A's
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + ownerTokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("owner@beta.example.com"));

        // Owner B attempts to deactivate Org A's user -> 400 Bad Request (not found in tenant)
        mockMvc.perform(delete("/api/v1/users/" + supervisorA.getId())
                        .header("Authorization", "Bearer " + ownerTokenB))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }
}
