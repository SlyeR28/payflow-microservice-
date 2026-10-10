package com.payflow.authservice.security.service;

import com.payflow.authservice.model.entity.User;
import com.payflow.authservice.model.entity.UserCredentials;
import com.payflow.authservice.model.enums.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final UserCredentials userCredentials;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }


    @Override
    public @Nullable String getPassword() {
        return  userCredentials.getPassword();
    }

    @Override
    public @NonNull String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getUserStatus() != UserStatus.DEACTIVATED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getLockedUntil() == null
                || user.getLockedUntil().isBefore(Instant.now());
    }

    /**
     * Credentials valid — check password age if you want.
     * Currently always true; add password expiry policy here later.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Account enabled — email must be verified.
     */
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.getEmailVerified())
                && user.getUserStatus() == UserStatus.ACTIVE;
    }
}
