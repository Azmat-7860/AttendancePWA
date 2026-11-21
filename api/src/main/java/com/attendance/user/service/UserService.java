package com.attendance.user.service;

import com.attendance.user.dto.InviteUserRequest;
import com.attendance.user.dto.UserResponse;
import com.attendance.user.entity.Role;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse inviteUser(InviteUserRequest request);

    List<UserResponse> listUsers();

    UserResponse updateUserRole(UUID userId, Role newRole);

    UserResponse deactivateUser(UUID userId);

    UserResponse reactivateUser(UUID userId);
}
