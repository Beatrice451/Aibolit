package org.beatrice.diploma_new_pharmacy.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.security.SecurityUser;
import org.beatrice.diploma_new_pharmacy.domain.user.dto.UserResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.mapper.UserMapper;
import org.beatrice.diploma_new_pharmacy.domain.user.model.Role;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.RoleRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.UserRepository;
import org.beatrice.diploma_new_pharmacy.exception.NotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public @Nullable UserResponse whoami(SecurityUser user) {
        if (user != null) {
            return userMapper.toDto(user.user());
        }

        return null;
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Role getRoleById(Integer roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));
    }

    @SuppressWarnings("RedundantCollectionOperation")
    @Transactional
    public UserResponse addUserRole(Integer userId, Integer roleId) {
        User user = getUserById(userId);
        Role role = getRoleById(roleId);
        if (!user.getUserRoles().contains(role)) {
            user.getUserRoles().add(role);
        }
        return userMapper.toDto(user);
    }

    @Transactional
    public UserResponse deleteUserRole(Integer userId, Integer roleId) {
        User user = getUserById(userId);
        Role role = getRoleById(roleId);
        user.getUserRoles().remove(role);
        return userMapper.toDto(user);
    }
}
