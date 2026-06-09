package com.example.authservice.security;

import com.example.authservice.permission.Permission;
import com.example.authservice.role.Role;
import com.example.authservice.user.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AuthenticatedUser(User user, List<GrantedAuthority> authorities) implements UserDetails {

    public static AuthenticatedUser from(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> roleAuthorities(role).stream())
                .distinct()
                .toList();
        return new AuthenticatedUser(user, authorities);
    }

    private static List<GrantedAuthority> roleAuthorities(Role role) {
        List<GrantedAuthority> permissionAuthorities = role.getPermissions().stream()
                .map(Permission::getName)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
        List<GrantedAuthority> authorities = new java.util.ArrayList<>(permissionAuthorities);
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        return authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
