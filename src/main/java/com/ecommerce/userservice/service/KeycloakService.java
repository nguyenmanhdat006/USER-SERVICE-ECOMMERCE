package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.LoginRequest;
import com.ecommerce.userservice.dto.request.RegisterRequest;
import com.ecommerce.userservice.dto.response.AuthResponse;
import com.ecommerce.userservice.exception.AuthenticationException;
import com.ecommerce.userservice.exception.UserAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    private final Keycloak keycloakAdminClient;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createUser(RegisterRequest request) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> existingUsers = usersResource.search(request.getEmail());
            if (!existingUsers.isEmpty()) {
                throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
            }

            UserRepresentation user = new UserRepresentation();
            user.setUsername(request.getEmail());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFullName().split(" ")[0]);
            user.setLastName(request.getFullName().contains(" ")
                    ? request.getFullName().substring(request.getFullName().indexOf(" ") + 1)
                    : "");
            user.setEnabled(true);
            user.setEmailVerified(true);

            user.setRequiredActions(Collections.emptyList());
            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                log.error("Failed to create user in Keycloak. Status: {}", response.getStatus());
                throw new RuntimeException("Failed to create user in Keycloak");
            }

            String locationHeader = response.getHeaderString("Location");
            String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(false);

            usersResource.get(userId).resetPassword(credential);

            assignRoleToUser(userId, "CUSTOMER");

            log.info("User created successfully in Keycloak with ID: {}", userId);
            return userId;

        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating user in Keycloak", e);
            throw new RuntimeException("Failed to create user in Keycloak: " + e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            String tokenEndpoint = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("username", request.getEmail());
            body.add("password", request.getPassword());
            body.add("scope", "openid profile email");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenEndpoint,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();

                return AuthResponse.builder()
                        .accessToken((String) tokenResponse.get("access_token"))
                        .refreshToken((String) tokenResponse.get("refresh_token"))
                        .expiresIn(((Number) tokenResponse.get("expires_in")).longValue())
                        .tokenType((String) tokenResponse.get("token_type"))
                        .build();
            }

            throw new AuthenticationException("Invalid credentials");

        } catch (Exception e) {
            log.error("Error during login", e);
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        try {
            String tokenEndpoint = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenEndpoint,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();

                return AuthResponse.builder()
                        .accessToken((String) tokenResponse.get("access_token"))
                        .refreshToken((String) tokenResponse.get("refresh_token"))
                        .expiresIn(((Number) tokenResponse.get("expires_in")).longValue())
                        .tokenType((String) tokenResponse.get("token_type"))
                        .build();
            }

            throw new AuthenticationException("Failed to refresh token");

        } catch (Exception e) {
            log.error("Error refreshing token", e);
            throw new AuthenticationException("Failed to refresh token: " + e.getMessage());
        }
    }

    public void logout(String refreshToken) {
        try {
            String logoutEndpoint = serverUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            restTemplate.exchange(
                    logoutEndpoint,
                    HttpMethod.POST,
                    entity,
                    Void.class);

            log.info("User logged out successfully");

        } catch (Exception e) {
            log.error("Error during logout", e);
        }
    }

    public void assignRoleToUser(String userId, String roleName) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realm);

            // Get role
            var role = realmResource.roles().get(roleName).toRepresentation();

            // Assign role to user
            realmResource.users().get(userId).roles().realmLevel()
                    .add(Collections.singletonList(role));

            log.info("Role {} assigned to user {}", roleName, userId);

        } catch (Exception e) {
            log.error("Error assigning role to user", e);
            throw new RuntimeException("Failed to assign role: " + e.getMessage());
        }
    }

    public UserRepresentation getUserById(String userId) {
        try {
            return keycloakAdminClient.realm(realm).users().get(userId).toRepresentation();
        } catch (Exception e) {
            log.error("Error getting user from Keycloak", e);
            throw new RuntimeException("Failed to get user: " + e.getMessage());
        }
    }

    public UserRepresentation findUserByEmail(String email) {
        try {
            List<UserRepresentation> users = keycloakAdminClient.realm(realm)
                    .users().searchByEmail(email, true); // exact match
            return users.isEmpty() ? null : users.get(0);
        } catch (Exception e) {
            log.error("Error searching Keycloak user by email={}", email, e);
            throw new RuntimeException("Failed to find user by email: " + e.getMessage());
        }
    }

    public void updateUser(String userId, UserRepresentation user) {
        try {
            keycloakAdminClient.realm(realm).users().get(userId).update(user);
            log.info("User {} updated successfully in Keycloak", userId);
        } catch (Exception e) {
            log.error("Error updating user in Keycloak", e);
            throw new RuntimeException("Failed to update user: " + e.getMessage());
        }
    }

    public void deleteUser(String userId) {
        try {
            keycloakAdminClient.realm(realm).users().get(userId).remove();
            log.info("User {} deleted from Keycloak", userId);
        } catch (Exception e) {
            log.error("Error deleting user from Keycloak", e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }

    public AuthResponse exchangeAuthorizationCode(String code, String redirectUri) {
        try {
            String tokenEndpoint = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("code", code);
            body.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenEndpoint,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();

                return AuthResponse.builder()
                        .accessToken((String) tokenResponse.get("access_token"))
                        .refreshToken((String) tokenResponse.get("refresh_token"))
                        .expiresIn(((Number) tokenResponse.get("expires_in")).longValue())
                        .tokenType((String) tokenResponse.get("token_type"))
                        .idToken((String) tokenResponse.get("id_token"))
                        .build();
            }

            throw new AuthenticationException("Failed to exchange authorization code");

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error exchanging authorization code with Keycloak", e);
            throw new AuthenticationException("Social login failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public String getEmailFromIdToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new AuthenticationException("Invalid id_token format");
            }

            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> claims = mapper.readValue(payloadJson, Map.class);

            String email = (String) claims.get("email");
            if (email == null || email.isBlank()) {
                throw new AuthenticationException("Email claim not found in id_token");
            }

            return email;
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to decode id_token", e);
            throw new AuthenticationException("Could not extract email from id_token: " + e.getMessage());
        }
    }
}