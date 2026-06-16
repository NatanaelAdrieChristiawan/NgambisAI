package com.ngambis.ai.services;

import com.ngambis.ai.dtos.request.UpdateProfileRequest;
import com.ngambis.ai.dtos.request.UserRequest;
import com.ngambis.ai.dtos.response.UserResponse;
import com.ngambis.ai.exceptions.ResourceNotFoundException;
import com.ngambis.ai.models.User;
import com.ngambis.ai.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling user CRUD operations and Spring Security UserDetailsService.
 *
 * <p>Supports both username-based and email-based lookups to accommodate
 * LOCAL (username) and GOOGLE (email) authentication providers.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username for Spring Security authentication.
     * Falls back to email lookup for Google OAuth2 users who may not have a separate username.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username or email: " + usernameOrEmail));
    }

    /**
     * Retrieves all users with pagination.
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    /**
     * Retrieves a user by ID, always reading from DB.
     * Used by /api/auth/me so caching is intentionally avoided here to prevent stale
     * name/profilePicture after an OAuth2 update.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return toResponse(user);
    }

    /**
     * Finds user entity by ID (for internal use).
     */
    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Updates user's last login timestamp.
     */
    @CacheEvict(value = "users", key = "#userId")
    @Transactional
    public void updateLastLogin(UUID userId) {
        User user = findById(userId);
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Updates a user's profile (name, username, profilePicture).
     * Validates username uniqueness before update.
     */
    @CacheEvict(value = "users", key = "#userId")
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findById(userId);

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username already taken: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }

        User saved = userRepository.save(user);
        log.info("Profile updated for user: {}", saved.getId());
        return toResponse(saved);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .provider(user.getProvider() != null ? user.getProvider().name() : null)
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
