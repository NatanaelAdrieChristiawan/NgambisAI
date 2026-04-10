package com.ngambis.ai.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * DTO for authentication responses containing JWT tokens and user profile.
 * Used for both LOCAL login/register and Google OAuth2 callback.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private UUID userId;
    private String username;
    private String email;
    private String name;
    private String profilePicture;
    private String provider;
    private Set<String> roles;
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
}
