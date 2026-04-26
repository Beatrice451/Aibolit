package org.beatrice.diploma_new_pharmacy.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.security.SecurityUser;
import org.beatrice.diploma_new_pharmacy.domain.user.dto.RoleResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.dto.UserResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.mapper.RoleMapper;
import org.beatrice.diploma_new_pharmacy.domain.user.mapper.UserMapper;
import org.beatrice.diploma_new_pharmacy.domain.user.model.Role;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.RoleRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.UserRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.specification.UserFilter;
import org.beatrice.diploma_new_pharmacy.domain.user.specification.UserSpecifications;
import org.beatrice.diploma_new_pharmacy.exception.NotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

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

    @PreAuthorize("hasRole('ADMIN')")
    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .toList();
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


    @Transactional
    public void deleteUser(Integer userId) {
        User user = getUserById(userId);
        user.setIsDeleted(true);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void restoreUser(Integer userId) { // TODO Письмо о восстановлении аккаунта?
        User user = getUserById(userId);
        user.setIsDeleted(false);
        user.setDeletedAt(null);
        userRepository.save(user);

    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getUsers(UserFilter filter, Pageable pageable) {
        Specification<User> spec = buildSpecification(filter);
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(userMapper::toDto);
    }

    private Specification<User> buildSpecification(UserFilter filter) {
        Specification<User> spec = (
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction()
        );

        if (filter.role() != null) {
            spec = spec.and(UserSpecifications.hasRole(filter.role()));
        }

        if (filter.isDeleted() != null) {
            spec = spec.and(UserSpecifications.hasIsDeleted(filter.isDeleted()));
        }

        if (filter.email() != null) {
            spec = spec.and(UserSpecifications.hasEmail(filter.email()));
        }

        if (filter.name() != null) {
            spec = spec.and(UserSpecifications.hasNameLike(filter.name()));
        }

        return spec;
    }
}
