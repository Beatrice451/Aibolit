package org.beatrice.diploma_new_pharmacy.auth;

import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class SecurityUser implements UserDetails {
    private final User user;

    public SecurityUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getUserRoles()
                .stream()
                .map(ur -> new SimpleGrantedAuthority(("ROLE_" + ur.getRole().getRoleName())))
                .toList();
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public @NonNull String getUsername() {
        return user.getEmail();
    }
}
