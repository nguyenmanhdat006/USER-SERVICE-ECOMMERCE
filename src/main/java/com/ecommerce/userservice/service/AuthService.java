package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.LoginRequest;
import com.ecommerce.userservice.dto.request.RegisterRequest;
import com.ecommerce.userservice.dto.response.AuthResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.exception.UserAlreadyExistsException;
import com.ecommerce.userservice.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
