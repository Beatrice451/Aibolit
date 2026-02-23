package org.beatrice.diploma_new_pharmacy.user.service;

import org.beatrice.diploma_new_pharmacy.auth.service.AuthService;
import org.beatrice.diploma_new_pharmacy.user.dto.UserRegistrationRequest;
import org.beatrice.diploma_new_pharmacy.user.model.Role;
import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.beatrice.diploma_new_pharmacy.user.model.UserRole;
import org.beatrice.diploma_new_pharmacy.user.repository.RoleRepository;
import org.beatrice.diploma_new_pharmacy.user.repository.UserRepository;
import org.beatrice.diploma_new_pharmacy.user.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private UserRegistrationRequest userRegistrationRequest;

    @BeforeEach
    void setUp() {
        userRegistrationRequest = new UserRegistrationRequest(
                "Ivan", "qwerty@gmail.com", "88005553535", "12345678"
        );
    }

    @Test
    void registerUser_shouldCreateUserSuccessfully() {
        when(userRepository.findUserByEmail(userRegistrationRequest.email()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(userRegistrationRequest.password()))
                .thenReturn("encodedPassword");

        Role role = new Role();
        role.setId(1);
        role.setRoleName("USER");

        when(roleRepository.findByRoleName("USER"))
                .thenReturn(Optional.of(role));

        authService.registerUser(userRegistrationRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(userRegistrationRequest.email());
        assertThat(savedUser.getPasswordHash()).isEqualTo("encodedPassword");

    }
}