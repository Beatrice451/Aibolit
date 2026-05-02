package org.beatrice.diploma_new_pharmacy.auth.integration;

import org.beatrice.diploma_new_pharmacy.config.BaseIntegrationTest;
import org.beatrice.diploma_new_pharmacy.domain.auth.dto.AuthRequest;
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
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Authorization Integration Tests")
class AuthorizationTest extends BaseIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String ADMIN_USERS_ENDPOINT = "/api/admin/user";
    private static final String USER_PROFILE_ENDPOINT = "/api/user/profile";
    
    private static final String REGULAR_USER_EMAIL = "user@example.com";
    private static final String ADMIN_USER_EMAIL = "admin@example.com";
    private static final String TEST_PASSWORD = "SecurePassword123!";

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

        // Given: USER and ADMIN roles exist
        Role userRole = TestDataFactory.createRole("USER");
        Role adminRole = TestDataFactory.createRole("ADMIN");
        roleRepository.save(userRole);
        roleRepository.save(adminRole);

        // Given: Regular user exists
        User regularUser = TestDataFactory.createUser(
                REGULAR_USER_EMAIL,
                "+79001111111",
                passwordEncoder.encode(TEST_PASSWORD)
        );
        regularUser.setUserRoles(Set.of(userRole));
        userRepository.save(regularUser);

        // Given: Admin user exists
        User adminUser = TestDataFactory.createUser(
                ADMIN_USER_EMAIL,
                "+79002222222",
                passwordEncoder.encode(TEST_PASSWORD)
        );
        adminUser.setUserRoles(Set.of(userRole, adminRole));
        userRepository.save(adminUser);

        // Given: REST client configured
        restClient = RestTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("TC-13: Should reject access to protected endpoint when no token provided")
    void shouldRejectAccess_whenNoTokenProvided() {
        // Given: No authentication token

        // When: User attempts to access protected endpoint without token
        restClient.get()
                .uri(USER_PROFILE_ENDPOINT)
                .exchange()
                // Then: Access is denied with 401 Unauthorized
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("TC-14: Should reject access to admin endpoint when user has no ADMIN role")
    void shouldRejectAccess_whenUserHasNoAdminRole() {
        // Given: Regular user is logged in (has USER role but not ADMIN)
        AuthRequest loginRequest = TestDataFactory.createValidAuthRequest(REGULAR_USER_EMAIL, TEST_PASSWORD);
        
        final String[] accessToken = new String[1];
        
        restClient.post()
                .uri(LOGIN_ENDPOINT)
                .body(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    accessToken[0] = extractAccessToken(result.getResponseBody());
                });

        // When: Regular user attempts to access admin endpoint
        restClient.get()
                .uri(ADMIN_USERS_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken[0])
                .exchange()
                // Then: Access is denied with 403 Forbidden
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("TC-15: Should allow access to admin endpoint when user has ADMIN role")
    void shouldAllowAccess_whenUserHasAdminRole() {
        // Given: Admin user is logged in (has both USER and ADMIN roles)
        AuthRequest loginRequest = TestDataFactory.createValidAuthRequest(ADMIN_USER_EMAIL, TEST_PASSWORD);
        
        final String[] accessToken = new String[1];
        
        restClient.post()
                .uri(LOGIN_ENDPOINT)
                .body(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    accessToken[0] = extractAccessToken(result.getResponseBody());
                });

        // When: Admin user attempts to access admin endpoint
        restClient.get()
                .uri(ADMIN_USERS_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken[0])
                .exchange()
                // Then: Access is granted with 200 OK
                .expectStatus().isOk();
    }

    /**
     * Helper method to extract access token from JSON response body.
     */
    private String extractAccessToken(String responseBody) {
        if (responseBody == null) return null;
        Pattern pattern = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(responseBody);
        return matcher.find() ? matcher.group(1) : null;
    }
}
