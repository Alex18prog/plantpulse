package com.plantpulse.security;

import com.plantpulse.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/** Wraps our User entity so the full domain object (including the linked Technician) is reachable from @AuthenticationPrincipal. */
public class AppUserPrincipal implements UserDetails {

    private final User user;

    public AppUserPrincipal(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
