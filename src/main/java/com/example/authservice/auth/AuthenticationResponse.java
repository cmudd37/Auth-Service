package com.example.authservice.auth;

public record AuthenticationResponse(String accessToken, String refreshToken, UserResponse user) {
}
