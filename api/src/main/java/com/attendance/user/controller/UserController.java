package com.attendance.user.controller;

import com.attendance.user.dto.InviteUserRequest;
import com.attendance.user.dto.UpdateUserRoleRequest;
import com.attendance.user.dto.UserResponse;
import com.attendance.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/invite")
    public ResponseEntity<UserResponse> inviteUser(@Valid @RequestBody InviteUserRequest request) {
        UserResponse response = userService.inviteUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = userService.listUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        UserResponse response = userService.updateUserRole(id, request.role());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable UUID id) {
        UserResponse response = userService.deactivateUser(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<UserResponse> reactivateUser(@PathVariable UUID id) {
        UserResponse response = userService.reactivateUser(id);
        return ResponseEntity.ok(response);
    }
}
