package org.beatrice.diploma_new_pharmacy.user.service;

import org.beatrice.diploma_new_pharmacy.user.dto.UserRegistrationRequest;
import org.beatrice.diploma_new_pharmacy.user.exception.UserAlreadyExistsException;
import org.beatrice.diploma_new_pharmacy.user.model.Role;
import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.beatrice.diploma_new_pharmacy.user.model.UserRole;
import org.beatrice.diploma_new_pharmacy.user.repository.RoleRepository;
import org.beatrice.diploma_new_pharmacy.user.repository.UserRepository;
import org.beatrice.diploma_new_pharmacy.user.repository.UserRoleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserRegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public UserRegistrationService(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   RoleRepository roleRepository,
                                   UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public void registerUser(UserRegistrationRequest request) {
        IO.println(request);
        userRepository.findUserByEmail(request.email())
                .ifPresent(_ -> {
                    throw new UserAlreadyExistsException("User with stated email already exists: " + request.email());
                });
        User user = new User();
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        IO.println(user);
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) { // ловим исключение на случай гонки
//            throw new UserAlreadyExistsException("User with stated email/phone already exists: " + request.email());
            throw new UserAlreadyExistsException(e.getMessage());
        }
        Role role = roleRepository.findByRoleName("USER").orElseThrow(
                () -> new IllegalStateException("Role USER not found")
        );

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);

        userRoleRepository.save(userRole);
    }


}
