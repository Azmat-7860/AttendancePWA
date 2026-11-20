package com.attendance.auth.service;

import com.attendance.auth.dto.AuthResult;

public interface AuthService {

    AuthResult loginWithGoogle(String idToken);

    AuthResult rotateRefreshToken(String rawRefreshToken);

    void logout(String rawRefreshToken);
}
