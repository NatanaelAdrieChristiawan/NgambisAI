package com.ngambis.ai.controllers;

import com.ngambis.ai.dtos.request.LoginRequest;
import com.ngambis.ai.dtos.request.RegisterRequest;
import com.ngambis.ai.dtos.response.ApiResponse;
import com.ngambis.ai.dtos.response.AuthResponse;
import com.ngambis.ai.dtos.response.UserResponse;
import com.ngambis.ai.models.User;
import com.ngambis.ai.services.AuthService;
import com.ngambis.ai.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints (register, login, refresh, profile).
 *
 * <p>Supports dual authentication:
 * <ul>
 *   <li>POST /api/auth/register — Manual registration (LOCAL)</li>
 *   <li>POST /api/auth/login — Manual login (LOCAL)</li>
 *   <li>POST /api/auth/refresh — Refresh JWT token</li>
 *   <li>GET /api/auth/me — Get current authenticated user's profile</li>
 * </ul>
 * Google OAuth2 login is initiated via <code>/oauth2/authorization/google</code>.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and registration APIs")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with LOCAL provider and returns JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());
        
        AuthResponse response = authService.register(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user credentials and returns JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());
        
        AuthResponse response = authService.login(request);
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestParam String refreshToken) {
        log.info("Token refresh request received");
        
        AuthResponse response = authService.refreshToken(refreshToken);
        
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    /**
     * Retrieves the authenticated user's profile.
     * Works for both LOCAL and GOOGLE users.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns the authenticated user's profile information")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        log.info("Profile request for user: {}", user.getUsername());

        // Reload from DB to get the freshest data (name, profilePicture, etc.)
        // @AuthenticationPrincipal is loaded once during JWT validation and may be stale
        UserResponse response = userService.getUserById(user.getId());

        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", response));
    }
}
