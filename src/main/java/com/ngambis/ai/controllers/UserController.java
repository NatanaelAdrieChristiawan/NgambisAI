package com.ngambis.ai.controllers;

import com.ngambis.ai.dtos.request.UpdateProfileRequest;
import com.ngambis.ai.dtos.response.ApiResponse;
import com.ngambis.ai.dtos.response.UserResponse;
import com.ngambis.ai.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for user management operations.
 * Requires authentication for all endpoints.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User profile and management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserService userService;

    /**
     * Get all users with pagination (admin only).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves paginated list of all users (Admin only)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/users — Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<UserResponse> users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully.", users));
    }

    /**
     * Get a user by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves user details by user ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {

        log.info("GET /api/users/{}", id);
        UserResponse user = userService.getUserById(id);

        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully.", user));
    }

    /**
     * Update user profile (name, username, profilePicture).
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Updates user's name, username, or profile picture")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("PUT /api/users/{} — Update profile", id);
        UserResponse updated = userService.updateProfile(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully.", updated));
    }
}

