package com.example.authservice.security;

import com.example.authservice.auth.AuthenticationResponse;
import com.example.authservice.auth.UserResponse;
import com.example.authservice.role.Role;
import com.example.authservice.role.RoleRepository;
import com.example.authservice.token.RefreshTokenService;
import com.example.authservice.user.User;
import com.example.authservice.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final String successRedirectUri;

    public OAuth2LoginSuccessHandler(
            UserRepository userRepository,
            RoleRepository roleRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            @Value("${app.oauth2.success-redirect-uri}") String successRedirectUri
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.successRedirectUri = successRedirectUri;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        User user = upsertUser(oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getPrincipal());
        AuthenticatedUser principal = AuthenticatedUser.from(user);
        AuthenticationResponse tokens = new AuthenticationResponse(
                jwtService.createAccessToken(principal),
                refreshTokenService.create(user.getId()),
                UserResponse.from(user)
        );

        String redirect = successRedirectUri
                + "?access_token=" + encode(tokens.accessToken())
                + "&refresh_token=" + encode(tokens.refreshToken());
        response.sendRedirect(redirect);
    }

    private User upsertUser(String provider, OAuth2User oauthUser) {
        String email = extractEmail(oauthUser.getAttributes());
        String providerId = oauthUser.getName();
        String displayName = String.valueOf(oauthUser.getAttributes().getOrDefault("name", email));
        Role userRole = roleRepository.findByName("USER").orElseThrow();
        return userRepository.findByEmail(email.toLowerCase())
                .orElseGet(() -> userRepository.save(User.oauth(email, displayName, provider, providerId, userRole)));
    }

    private String extractEmail(Map<String, Object> attributes) {
        Object email = attributes.get("email");
        if (email == null || email.toString().isBlank()) {
            throw new IllegalStateException("OAuth2 provider did not return an email address");
        }
        return email.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
