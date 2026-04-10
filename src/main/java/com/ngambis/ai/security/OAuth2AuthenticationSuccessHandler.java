package com.ngambis.ai.security;

import com.ngambis.ai.models.User;
import com.ngambis.ai.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Handler invoked after successful Google OAuth2 authentication.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Retrieves the authenticated user from the database by email</li>
 *   <li>Generates JWT access and refresh tokens</li>
 *   <li>Updates the user's last login timestamp</li>
 *   <li>Redirects to the frontend with tokens as URL parameters</li>
 * </ol>
 * </p>
 *
 * <p>The frontend captures these tokens from the URL and stores them
 * in localStorage for subsequent API calls.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2/callback}")
    private String frontendRedirectUri;

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("🔗 OAuth2 frontend redirect URI: {}", frontendRedirectUri);
    }

    /**
     * Called when OAuth2 authentication succeeds.
     * Generates JWT tokens and redirects to frontend with tokens in URL.
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        log.info("OAuth2 authentication successful for: {}", email);

        // Fetch user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "User not found after OAuth2 authentication: " + email));

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("JWT tokens generated for OAuth2 user: {}", email);

        // Build redirect URL with tokens as query parameters
        String targetUrl = frontendRedirectUri
                + "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                + "&userId=" + URLEncoder.encode(user.getId().toString(), StandardCharsets.UTF_8)
                + "&email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                + "&name=" + URLEncoder.encode(user.getName() != null ? user.getName() : "", StandardCharsets.UTF_8);

        log.info("Redirecting OAuth2 user to: {}", frontendRedirectUri);
        log.debug("Full redirect URL: {}", targetUrl);

        if (response.isCommitted()) {
            log.warn("Response already committed. Unable to redirect to {}", targetUrl);
            return;
        }

        // Clear authentication attributes from session
        clearAuthenticationAttributes(request);

        // Use response.sendRedirect directly instead of getRedirectStrategy()
        // to ensure absolute URL is honored (including port 5173)
        response.sendRedirect(targetUrl);
    }
}
