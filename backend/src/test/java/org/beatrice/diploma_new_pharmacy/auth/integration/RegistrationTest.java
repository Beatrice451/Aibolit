package org.beatrice.diploma_new_pharmacy.auth.integration;

import org.beatrice.diploma_new_pharmacy.domain.auth.dto.UserRegistrationRequest;
import org.beatrice.diploma_new_pharmacy.domain.user.model.Role;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.beatrice.diploma_new_pharmacy.domain.user.model.UserRole;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.RoleRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.UserRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
public class RegistrationTest {


    private static final String REGISTER_ENDPOINT_URI = "/api/auth/register";

    private final RestTestClient http = RestTestClient
            .bindToServer()
            .baseUrl("http://localhost:8080")
            .build();

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;

    @BeforeEach
    void setup() {
        userRoleRepository.deleteAll();
        roleRepository.deleteAll();
        userRepository.deleteAll();
        Role role = new Role();
        role.setRoleName("USER");
        roleRepository.save(role);

        User user = new User();
        user.setPhone("88888888888");
        user.setEmail("conflict@test.com");
        user.setPasswordHash("qwerty");
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        userRole.setUser(user);
        user.setUserRoles(Set.of(userRole));
        userRepository.save(user);
    }


    @Test
    void register_shouldRegisterUser_whenValidRequestProvided() {
        UserRegistrationRequest request = new UserRegistrationRequest("test", "test@test.test", "88005553535", "password");
        http.post()
                .uri(REGISTER_ENDPOINT_URI)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().isEmpty();

        assertTrue(userRepository.findUserByEmail("test@test.test").isPresent());
        assertTrue(userRepository.findUserByPhone("88005553535").isPresent());
        assertNotEquals("password", userRepository.findUserByPhone("88005553535").get().getPasswordHash());

    }

    @Test
    void register_shouldReturnConflictStatus_withUserWithEmailAlreadyExists() {


        UserRegistrationRequest request = new UserRegistrationRequest("test", "conflict@test.com", "88005553535", "password");
        http.post()
                .uri(REGISTER_ENDPOINT_URI)
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody();

        assertTrue(userRepository.findUserByPhone("88005553535").isEmpty());
    }

    @Test
    void register_shouldReturnConflictStatus_withUserWithPhoneAlreadyExists() {
        UserRegistrationRequest request = new UserRegistrationRequest("test", "unique@test.com", "88888888888", "password");
        http.post()
                .uri(REGISTER_ENDPOINT_URI)
                .body(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        assertTrue(userRepository.findUserByEmail("unique@test.com").isEmpty());
    }
}
