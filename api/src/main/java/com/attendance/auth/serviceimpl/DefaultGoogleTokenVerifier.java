package com.attendance.auth.serviceimpl;

import com.attendance.auth.dto.GoogleUserInfo;
import com.attendance.auth.service.GoogleTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DefaultGoogleTokenVerifier implements GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(DefaultGoogleTokenVerifier.class);

    private final GoogleIdTokenVerifier verifier;

    public DefaultGoogleTokenVerifier(@Value("${app.auth.google-client-id}") String googleClientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .setIssuers(List.of("https://accounts.google.com", "accounts.google.com"))
                .build();
    }

    @Override
    public GoogleUserInfo verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.warn("Google ID token verification failed: null payload or signature invalid");
                throw new BadCredentialsException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            Boolean emailVerified = payload.getEmailVerified();
            if (emailVerified == null || !emailVerified) {
                log.warn("Google ID token rejected: email is not verified");
                throw new BadCredentialsException("Google email address is not verified");
            }

            String email = payload.getEmail();
            String sub = payload.getSubject();
            String name = (String) payload.get("name");
            if (name == null || name.isBlank()) {
                name = email;
            }

            return new GoogleUserInfo(sub, email, name, true);
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception during Google token verification", e);
            throw new BadCredentialsException("Failed to verify Google ID token: " + e.getMessage());
        }
    }
}
