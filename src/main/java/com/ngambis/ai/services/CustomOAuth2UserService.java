package com.ngambis.ai.services;

import com.ngambis.ai.models.AuthProvider;
import com.ngambis.ai.models.User;
import com.ngambis.ai.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * Custom OAuth2 user service for processing Google OAuth2 login.
 *
 * <p>Handles the flow after Google authenticates the user:
 * <ol>
 *   <li>Fetches user profile from Google API (email, name, picture)</li>
 *   <li>Checks if user already exists in database</li>
 *   <li>Creates new user if first-time Google login</li>
 *   <li>Updates existing user's profile information on subsequent logins</li>
 * </ol>
 * </p>
 *
 * <p>Implements the <b>Single Responsibility Principle</b>: this service only handles
 * OAuth2 user data processing, keeping it separate from JWT generation and local auth.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * Loads the OAuth2 user from Google and registers/updates in local database.
     *
     * @param userRequest the OAuth2 user request containing access token and client registration
     * @return the authenticated OAuth2User
     * @throws OAuth2AuthenticationException if authentication fails
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String googleId = oAuth2User.getAttribute("sub");

        log.info("OAuth2 login attempt for email: {}", email);

        // Check if user exists with this email
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // If user registered locally, link the Google account
            if (user.getProvider() == AuthProvider.LOCAL) {
                log.info("Linking Google account to existing LOCAL user: {}", email);
                user.setProvider(AuthProvider.GOOGLE);
                user.setProviderId(googleId);
                user.setProfilePicture(picture);
                if (user.getName() == null || user.getName().isEmpty()) {
                    user.setName(name);
                }
                userRepository.save(user);
            } else {
                // Update profile info for returning Google user
                log.info("Returning Google user: {}", email);
                user.setName(name);
                user.setProfilePicture(picture);
                userRepository.save(user);
            }
        } else {
            // Register new Google user
            log.info("Registering new Google user: {}", email);
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .username(email) // Use email as username for Google users
                    .provider(AuthProvider.GOOGLE)
                    .providerId(googleId)
                    .profilePicture(picture)
                    .roles(Set.of("USER"))
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(newUser);
            log.info("New Google user registered: {}", email);
        }

        return oAuth2User;
    }
}
