package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.RegisterRequest;
import com.ecommerce.userservice.dto.request.UpdateUserRequest;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.entity.UserPreference;
import com.ecommerce.userservice.exception.ResourceNotFoundException;
import com.ecommerce.userservice.mapper.UserMapper;
import com.ecommerce.userservice.repository.UserPreferenceRepository;
import com.ecommerce.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final KeycloakService keycloakService;
    private final UserMapper userMapper;

    /**
     * Create new user (called after Keycloak registration)
     */
    @Transactional
    public User createUser(RegisterRequest request, String keycloakId) {
        User user = userMapper.toEntity(request);
        user.setKeycloakId(keycloakId);

        User savedUser = userRepository.save(user);

        // Create default preferences
        UserPreference preference = UserPreference.builder()
                .user(savedUser)
                .build();
        userPreferenceRepository.save(preference);

        log.info("User created with ID: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Get current authenticated user
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        User user = getCurrentUserEntity();
        UserResponse response = userMapper.toResponse(user);
        response.setRoles(getCurrentUserRoles());
        return response;
    }

    /**
     * Get current user entity
     */
    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        String keycloakId = getCurrentKeycloakUserId();
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Update current user
     */
    @Transactional
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        User user = getCurrentUserEntity();

        // Update local database
        userMapper.updateEntityFromRequest(request, user);
        User updatedUser = userRepository.save(user);

        // Update Keycloak (optional - sync full name)
        try {
            UserRepresentation keycloakUser = keycloakService.getUserById(user.getKeycloakId());
            if (request.getFullName() != null) {
                String[] names = request.getFullName().split(" ", 2);
                keycloakUser.setFirstName(names[0]);
                keycloakUser.setLastName(names.length > 1 ? names[1] : "");
            }
            keycloakService.updateUser(user.getKeycloakId(), keycloakUser);
        } catch (Exception e) {
            log.warn("Failed to update user in Keycloak", e);
        }

        UserResponse response = userMapper.toResponse(updatedUser);
        response.setRoles(getCurrentUserRoles());
        return response;
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * Get user by Keycloak ID
     */
    @Transactional(readOnly = true)
    public User getUserByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloakId: " + keycloakId));
    }

    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Get current user's Keycloak ID from JWT
     */
    private String getCurrentKeycloakUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return jwt.getSubject(); // This is the Keycloak user ID
        }

        throw new RuntimeException("Unable to get current user ID");
    }

    /**
     * Get current user's roles from JWT
     */
    private List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(role -> role.startsWith("ROLE_"))
                    .map(role -> role.substring(5)) // Remove "ROLE_" prefix
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    /**
     * Delete user (soft delete - set status to INACTIVE)
     */
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(user);

        log.info("User {} marked as inactive", id);
    }

    /**
     * Get all users (admin only)
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userMapper.toResponseList(userRepository.findAll());
    }
}