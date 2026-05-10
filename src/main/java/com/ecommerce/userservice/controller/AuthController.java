package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.request.ForgotPasswordRequest;
import com.ecommerce.userservice.dto.request.LoginRequest;
import com.ecommerce.userservice.dto.request.OAuthRequest;
import com.ecommerce.userservice.dto.request.RegisterRequest;
import com.ecommerce.userservice.dto.response.ApiResponse;
import com.ecommerce.userservice.dto.response.AuthResponse;
import com.ecommerce.userservice.dto.response.UserResponse;
import com.ecommerce.userservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and Registration APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        try {
            UserResponse response = authService.register(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", response));
        } catch (Exception e) {
            // Preserve previous behavior: return 409 for existing user
            if (e instanceof com.ecommerce.userservice.exception.UserAlreadyExistsException) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("User with this email already exists"));
            }
            throw e;
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and get access token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/oauth2/{provider}")
    @Operation(
            summary = "Social login callback",
            description = "Exchange the Keycloak authorization code for tokens after social provider (Google, Facebook, etc.) authentication. "
                    + "Frontend must pass the code and the exact redirect_uri it used to initiate the flow.")
    public ResponseEntity<ApiResponse<AuthResponse>> socialLogin(
            @PathVariable String provider,
            @Valid @RequestBody OAuthRequest request) {
        log.info("Social login callback received for provider: {}", provider);
        AuthResponse authResponse = authService.socialLogin(request, provider);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Refresh token is required"));
        }

        AuthResponse authResponse = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout user and invalidate tokens")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        authService.logout(refreshToken);

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Forgot password",
            description = "Triggers Keycloak to send an execute-actions email (UPDATE_PASSWORD) when a user exists for this address. "
                    + "On success over HTTP this API always returns the same message whether or not the email is registered (no account enumeration)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Request accepted; if the email is registered and Keycloak can send mail, the user receives the reset flow link.",
                    content = @Content(
                            schema = @Schema(implementation = com.ecommerce.userservice.dto.response.ApiResponse.class)
                    )),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid payload (e.g. missing or invalid email format)",
                    content = @Content(
                            schema = @Schema(implementation = com.ecommerce.userservice.dto.response.ApiResponse.class)
                    )),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Keycloak or mail delivery failure after a matching user was found",
                    content = @Content(
                            schema = @Schema(implementation = com.ecommerce.userservice.dto.response.ApiResponse.class)
                    ))
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("If the email exists, a password reset link has been sent", null));
    }
}