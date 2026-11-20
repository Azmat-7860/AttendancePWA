package com.attendance.auth.service;

import com.attendance.auth.dto.GoogleUserInfo;

public interface GoogleTokenVerifier {

    /**
     * Verifies a Google ID token's signature, issuer, audience, and expiry.
     *
     * @param idToken the raw JWT string issued by Google
     * @return verified Google user payload
     */
    GoogleUserInfo verify(String idToken);
}
