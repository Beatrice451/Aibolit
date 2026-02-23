package org.beatrice.diploma_new_pharmacy.auth.service;

import org.beatrice.diploma_new_pharmacy.auth.dto.AuthResponse;
import org.beatrice.diploma_new_pharmacy.auth.exception.PhoneAlreadyExistsException;
import org.beatrice.diploma_new_pharmacy.auth.exception.UserAlreadyExistsException;
import org.beatrice.diploma_new_pharmacy.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.user.dto.UserRegistrationRequest;
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
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenRevocationService tokenRevocationService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       JwtService jwtService, RefreshTokenService refreshTokenService, TokenRevocationService tokenRevocationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.tokenRevocationService = tokenRevocationService;
    }

    public void registerUser(UserRegistrationRequest request) {
        userRepository.findUserByEmail(request.email())
                .ifPresent(_ -> {
                    throw new UserAlreadyExistsException("User with stated email already exists: " + request.email());
                });

        if (userRepository.existsUserByPhone(request.phone())) {
            throw new PhoneAlreadyExistsException("User with stated phone already exists:" + request.phone());
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
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

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenService.validate(refreshToken);
        tokenRevocationService.revoke(stored);
        String accessToken = jwtService.generateAccessToken(stored.getUser());
        RefreshToken newToken = refreshTokenService.create(stored.getUser());
        return new AuthResponse(accessToken, newToken.getToken());
    }
}
