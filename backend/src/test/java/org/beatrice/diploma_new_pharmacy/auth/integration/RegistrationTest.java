package org.beatrice.diploma_new_pharmacy.auth.integration;

import org.beatrice.diploma_new_pharmacy.config.BaseIntegrationTest;
import org.beatrice.diploma_new_pharmacy.domain.auth.dto.UserRegistrationRequest;
import org.beatrice.diploma_new_pharmacy.domain.user.model.Role;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.RoleRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.UserRepository;
import org.beatrice.diploma_new_pharmacy.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Registration Integration Tests")
class RegistrationTest extends BaseIntegrationTest {

    private static final String REGISTER_ENDPOINT = "/api/auth/register";

    @LocalServerPort
    private int port;

    private RestTestClient restClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        // Given: USER role exists in database
        Role userRole = TestDataFactory.createRole("USER");
        roleRepository.save(userRole);

        // Given: REST client configured
        restClient = RestTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("TC-1: Should successfully register user when valid data provided")
    void shouldRegisterUser_whenValidDataProvided() {
        // Given: Valid registration request
        UserRegistrationRequest request = TestDataFactory.createValidRegistrationRequest();

        // When: User attempts to register
        restClient.post()
                .uri(REGISTER_ENDPOINT)
                .body(request)
                .exchange()
                // Then: Registration succeeds with 201 Created
                .expectStatus().isCreated()
                .expectBody().isEmpty();

        // Then: User exists in database
        assertThat(userRepository.findUserByEmail(request.email()))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getEmail()).isEqualTo(request.email());
                    assertThat(user.getPhone()).isEqualTo(request.phone());
                    // Then: Password is hashed, not stored in plain text
                    assertThat(user.getPasswordHash()).isNotEqualTo(request.password());
                    assertThat(passwordEncoder.matches(request.password(), user.getPasswordHash())).isTrue();
                });
    }

    @Test
    @DisplayName("TC-2: Should reject registration when email already exists")
    void shouldRejectRegistration_whenEmailAlreadyExists() {
        // Given: User with email already exists
        String existingEmail = "existing@example.com";
        User existingUser = TestDataFactory.createUser(existingEmail, "+79001234567", "hashedPassword");
        userRepository.save(existingUser);

        // Given: Registration request with existing email
        UserRegistrationRequest request = TestDataFactory.createRegistrationRequest(
                existingEmail,
                "+79009999999",
                "NewPassword123!",
                "first",
                "last"
        );

        // When: User attempts to register with existing email
        restClient.post()
                .uri(REGISTER_ENDPOINT)
                .body(request)
                .exchange()
                // Then: Registration fails with 409 Conflict
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        // Then: No new user with new phone created
        assertThat(userRepository.findUserByPhone("+79009999999")).isEmpty();
    }

    @Test
    @DisplayName("TC-3: Should reject registration when phone already exists")
    void shouldRejectRegistration_whenPhoneAlreadyExists() {
        // Given: User with phone already exists
        String existingPhone = "+79001234567";
        User existingUser = TestDataFactory.createUser("existing@example.com", existingPhone, "hashedPassword");
        userRepository.save(existingUser);

        // Given: Registration request with existing phone
        UserRegistrationRequest request = TestDataFactory.createRegistrationRequest(
                "newuser@example.com",
                existingPhone,
                "NewPassword123!",
                "first",
                "last"
        );

        // When: User attempts to register with existing phone
        restClient.post()
                .uri(REGISTER_ENDPOINT)
                .body(request)
                .exchange()
                // Then: Registration fails with 409 Conflict
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        // Then: No new user with new email created
        assertThat(userRepository.findUserByEmail("newuser@example.com")).isEmpty();
    }

    @Test
    @DisplayName("TC-4: Should reject registration when email format is invalid")
    void shouldRejectRegistration_whenEmailFormatIsInvalid() {
        // Given: Registration request with invalid email format
        UserRegistrationRequest request = TestDataFactory.createRegistrationRequest(
                TestDataFactory.createInvalidEmail(),
                "+79001234567",
                "ValidPassword123!",
                "first",
                "last"
        );

        // When: User attempts to register with invalid email
        restClient.post()
                .uri(REGISTER_ENDPOINT)
                .body(request)
                .exchange()
                // Then: Registration fails with 400 Bad Request (validation error)
                .expectStatus().isBadRequest();

        // Then: No user created in database
        assertThat(userRepository.findUserByPhone("+79001234567")).isEmpty();
    }

    @Test
    @DisplayName("TC-5: Should reject registration when password is too short")
    void shouldRejectRegistration_whenPasswordIsTooShort() {
        // Given: Registration request with short password
        UserRegistrationRequest request = TestDataFactory.createRegistrationRequest(
                "test@example.com",
                "+79001234567",
                TestDataFactory.createShortPassword(),
                "first",
                "last"
        );

        // When: User attempts to register with short password
        restClient.post()
                .uri(REGISTER_ENDPOINT)
                .body(request)
                .exchange()
                // Then: Registration fails with 400 Bad Request (validation error)
                .expectStatus().isBadRequest();

        // Then: No user created in database
        assertThat(userRepository.findUserByEmail("test@example.com")).isEmpty();
    }
}
