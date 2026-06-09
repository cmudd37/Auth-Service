package com.example.authservice.auth;

import com.example.authservice.permission.Permission;
import com.example.authservice.role.Role;
import com.example.authservice.user.User;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        Set<String> roles,
        Set<String> permissions
) {
    public static UserResponse from(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), roles, permissions);
    }
}
