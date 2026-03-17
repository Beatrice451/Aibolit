package org.beatrice.diploma_new_pharmacy.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.security.SecurityUser;
import org.beatrice.diploma_new_pharmacy.domain.user.dto.UserResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.mapper.UserMapper;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public @Nullable UserResponse whoami(SecurityUser user) {
        if (user != null) {
            return userMapper.toDto(user.user());
        }

        return null;
    }
}
