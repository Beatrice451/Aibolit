package org.beatrice.diploma_new_pharmacy.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.PhoneAlreadyExistsException;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.UserAlreadyExistsException;
import org.beatrice.diploma_new_pharmacy.domain.auth.service.model.RegistrationCommand;
import org.beatrice.diploma_new_pharmacy.domain.auth.util.PhoneNormalizer;
import org.beatrice.diploma_new_pharmacy.domain.user.model.Role;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.RoleRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.UserRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.service.EmailVerificationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailVerificationService emailVerificationService;


    @Transactional
    public void registerUser(RegistrationCommand cmd) {
        userRepository.findUserByEmail(cmd.email())
                .ifPresent(_ -> {
                    throw new UserAlreadyExistsException("User with stated email already exists: " + cmd.email());
                });

        String normalizedPhone = PhoneNormalizer.normalize(cmd.phone());
        if (userRepository.existsUserByPhone(normalizedPhone)) {
            throw new PhoneAlreadyExistsException("User with stated phone already exists: " + cmd.phone());
        }

        User user = User.builder()
                .email(cmd.email())
                .phone(cmd.phone())
                .passwordHash(passwordEncoder.encode(cmd.password()))
                .firstName(cmd.firstName())
                .lastName(cmd.lastName())
                .build();

        Role role = roleRepository.findByRoleName("USER").orElseThrow(
                () -> new IllegalStateException("Role USER not found")
        );

        user.addRole(role);

        userRepository.save(user);

        emailVerificationService.sendVerificationEmail(user);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("User with stated email/phone already exists: " + cmd.email());
        }
    }
}
