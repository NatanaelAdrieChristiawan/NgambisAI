package com.ngambis.ai.services;

import com.ngambis.ai.dtos.request.LoginRequest;
import com.ngambis.ai.dtos.request.RegisterRequest;
import com.ngambis.ai.dtos.response.AuthResponse;
import com.ngambis.ai.models.AuthProvider;
import com.ngambis.ai.models.User;
import com.ngambis.ai.repositories.UserRepository;
import com.ngambis.ai.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Service handling user authentication and registration.
 *
 * <p>Supports dual authentication:
 * <ul>
 *   <li><b>LOCAL</b>: Manual registration with email/password + BCrypt encryption</li>
 *   <li><b>GOOGLE</b>: OAuth2 flow handled by {@link CustomOAuth2UserService}
 *       and {@link com.ngambis.ai.security.OAuth2AuthenticationSuccessHandler}</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Registers a new user with LOCAL provider and default USER role.
     * Password is encrypted using BCryptPasswordEncoder.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .name(request.getUsername()) // Use username as display name
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .roles(Set.of("USER"))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return generateAuthResponse(savedUser);
    }

    /**
     * Authenticates a user with username/password and generates JWT tokens.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        
        // Update last login
        userService.updateLastLogin(user.getId());

        log.info("User logged in successfully: {}", user.getUsername());
        return generateAuthResponse(user);
    }

    /**
     * Refreshes an access token using a valid refresh token.
     */
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtTokenProvider.extractUsername(refreshToken);
        
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!jwtTokenProvider.validateToken(refreshToken, user)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        return generateAuthResponse(user);
    }

    /**
     * Generates authentication response with tokens.
     */
    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .provider(user.getProvider().name())
                .roles(user.getRoles())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .build();
    }
}
