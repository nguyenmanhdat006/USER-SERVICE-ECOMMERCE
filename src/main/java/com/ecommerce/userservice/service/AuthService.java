package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.LoginRequest;
import com.ecommerce.userservice.dto.request.OAuthRequest;
import com.ecommerce.userservice.dto.request.RegisterRequest;
import com.ecommerce.userservice.dto.response.AuthResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.exception.UserAlreadyExistsException;
import com.ecommerce.userservice.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final KeycloakService keycloakService;
    private final UserService userService;
    private final UserMapper userMapper;

    public UserResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getEmail());

        if (userService.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        String keycloakId = keycloakService.createUser(request);
        User user = userService.createUser(request, keycloakId);

        return userMapper.toResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Logging in user: {}", request.getEmail());

        AuthResponse authResponse = keycloakService.login(request);

        User user = userService.getUserByEmail(request.getEmail());
        authResponse.setUser(userMapper.toResponse(user));

        return authResponse;
    }

    public AuthResponse socialLogin(OAuthRequest request, String provider) {
        log.info("Social login initiated via provider: {}", provider);

        AuthResponse authResponse = keycloakService.exchangeAuthorizationCode(
                request.getCode(), request.getRedirectUri());

        String idToken = authResponse.getIdToken();
        if (idToken == null || idToken.isBlank()) {
            throw new com.ecommerce.userservice.exception.AuthenticationException(
                    "id_token not returned by Keycloak – ensure 'openid' scope is requested");
        }
        String email = keycloakService.getEmailFromIdToken(idToken);
        log.info("Social login: resolved email={} via provider={}", email, provider);

        if (!userService.existsByEmail(email)) {
            log.info("First social login for {}. Auto-provisioning local user record.", email);
            provisionSocialUser(email, provider);
        }

        User user = userService.getUserByEmail(email);
        authResponse.setUser(userMapper.toResponse(user));

        return authResponse;
    }

    private void provisionSocialUser(String email, String provider) {

        try {
            UserRepresentation kcUser = keycloakService.findUserByEmail(email);
            if (kcUser == null) {
                throw new RuntimeException("Keycloak user not found after social login for email: " + email);
            }

            RegisterRequest syntheticRequest = new RegisterRequest();
            syntheticRequest.setEmail(email);
            syntheticRequest.setFullName(buildFullName(kcUser));

            syntheticRequest.setPassword("SOCIAL_LOGIN_NO_PASSWORD_" + provider.toUpperCase());

            userService.createUser(syntheticRequest, kcUser.getId());
        } catch (Exception e) {
            log.error("Failed to provision social user for email={}", email, e);
            throw new com.ecommerce.userservice.exception.AuthenticationException(
                    "Could not provision user account: " + e.getMessage());
        }
    }

    private String buildFullName(UserRepresentation kcUser) {
        String firstName = kcUser.getFirstName() != null ? kcUser.getFirstName() : "";
        String lastName = kcUser.getLastName() != null ? kcUser.getLastName() : "";
        String full = (firstName + " " + lastName).trim();
        return full.isBlank() ? kcUser.getUsername() : full;
    }

    public AuthResponse refreshToken(String refreshToken) {
        return keycloakService.refreshToken(refreshToken);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            keycloakService.logout(refreshToken);
        }
    }

    public void forgotPassword(String email) {
        // TODO: Implement password reset email logic via Keycloak
        log.info("Forgot password requested for email: {}", email);
    }

    public void resetPassword(String token, String newPassword) {
        // TODO: Implement password reset logic via Keycloak API
        log.info("Reset password requested with token: {}", token);
    }
}
