package com.example.authservice.auth;

import com.example.authservice.role.Role;
import com.example.authservice.role.RoleRepository;
import com.example.authservice.security.AuthenticatedUser;
import com.example.authservice.security.JwtService;
import com.example.authservice.token.RefreshTokenService;
import com.example.authservice.user.User;
import com.example.authservice.user.UserRepository;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        Role userRole = roleRepository.findByName("USER").orElseThrow();
        User user = userRepository.save(User.local(
                email,
                passwordEncoder.encode(request.password()),
                request.displayName(),
                userRole
        ));
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.email().toLowerCase(),
                request.password()
        ));
        User user = userRepository.findByEmail(request.email().toLowerCase()).orElseThrow();
        return issueTokens(user);
    }

    @Transactional
    public AuthenticationResponse refresh(String refreshToken) {
        RefreshTokenService.Rotation rotation = refreshTokenService.rotate(refreshToken);
        User user = userRepository.findById(rotation.userId()).orElseThrow();
        return new AuthenticationResponse(
                jwtService.createAccessToken(AuthenticatedUser.from(user)),
                rotation.refreshToken(),
                UserResponse.from(user)
        );
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private AuthenticationResponse issueTokens(User user) {
        UUID userId = user.getId();
        return new AuthenticationResponse(
                jwtService.createAccessToken(AuthenticatedUser.from(user)),
                refreshTokenService.create(userId),
                UserResponse.from(user)
        );
    }
}
