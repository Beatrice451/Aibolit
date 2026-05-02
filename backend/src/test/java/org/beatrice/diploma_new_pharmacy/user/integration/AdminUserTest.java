package org.beatrice.diploma_new_pharmacy.user.integration;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Admin User Integration Tests")
class AdminUserTest extends BaseIntegrationTest {

    private static final String ADMIN_USER_ENDPOINT = "/api/admin/user";
    private static final String AUTH_ENDPOINT = "/api/auth/login";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String USER_EMAIL = "user@example.com";
    private static final String TEST_PASSWORD = "SecurePassword123!";

    @LocalServerPort
    private int port;

    private RestTestClient restClient;
    private String adminAccessToken;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        Role userRole = TestDataFactory.createRole("USER");
        Role adminRole = TestDataFactory.createRole("ADMIN");
        Role managerRole = TestDataFactory.createRole("MANAGER");
        roleRepository.save(userRole);
        roleRepository.save(adminRole);
        roleRepository.save(managerRole);

        User regularUser = TestDataFactory.createUser(
                USER_EMAIL,
                "+79001234567",
                passwordEncoder.encode(TEST_PASSWORD)
        );
        regularUser.setUserRoles(Set.of(userRole));
        userRepository.save(regularUser);

        User adminUser = TestDataFactory.createUser(
                ADMIN_EMAIL,
                "+79002222222",
                passwordEncoder.encode(TEST_PASSWORD)
        );
        adminUser.setUserRoles(Set.of(userRole, adminRole));
        userRepository.save(adminUser);

        restClient = RestTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        adminAccessToken = restClient.post()
                .uri(AUTH_ENDPOINT)
                .body(new AuthRequest(ADMIN_EMAIL, TEST_PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody();

        adminAccessToken = extractAccessToken(adminAccessToken);
    }

    private String extractAccessToken(String json) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private Integer extractUserId(String json) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    @Test
    @DisplayName("TC-56: Admin should get all users")
    void shouldGetAllUsers() {
        restClient.get()
                .uri(ADMIN_USER_ENDPOINT)
                .header("Authorization", "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).contains("\"content\"");
                });
    }

    @Test
    @DisplayName("TC-57: Admin should assign role to user")
    void shouldAssignRoleToUser() {
        final Integer[] userId = new Integer[1];

        restClient.get()
                .uri(ADMIN_USER_ENDPOINT + "?email=" + USER_EMAIL)
                .header("Authorization", "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    userId[0] = extractUserId(result.getResponseBody());
                });

        restClient.put()
                .uri(ADMIN_USER_ENDPOINT + "/" + userId[0] + "/roles/3")
                .header("Authorization", "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).contains("MANAGER");
                });
    }

    @Test
    @DisplayName("TC-58: Admin should remove role from user")
    void shouldRemoveRoleFromUser() {
        final Integer[] userId = new Integer[1];

        restClient.get()
                .uri(ADMIN_USER_ENDPOINT + "?email=" + USER_EMAIL)
                .header("Authorization", "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    userId[0] = extractUserId(result.getResponseBody());
                });

        restClient.delete()
                .uri(ADMIN_USER_ENDPOINT + "/" + userId[0] + "/roles/1")
                .header("Authorization", "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).doesNotContain("USER");
                });
    }

    @Test
    @DisplayName("TC-59: Admin should restore deleted user")
    void shouldRestoreDeletedUser() {
        final Integer[] userId = new Integer[1];

        restClient.get()
                .uri(ADMIN_USER_ENDPOINT + "?email=" + USER_EMAIL)
                .header("Authorization", "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    userId[0] = extractUserId(result.getResponseBody());
                });

        restClient.delete()
                .uri(ADMIN_USER_ENDPOINT + "/" + userId[0])
                .header("Authorization", "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus().isNoContent();

        restClient.patch()
                .uri(ADMIN_USER_ENDPOINT + "/" + userId[0] + "/restore")
                .header("Authorization", "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("TC-60: Admin should soft delete user")
    void shouldSoftDeleteUser() {
        final Integer[] userId = new Integer[1];

        restClient.get()
                .uri(ADMIN_USER_ENDPOINT + "?email=" + USER_EMAIL)
                .header("Authorization", "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    userId[0] = extractUserId(result.getResponseBody());
                });

        restClient.delete()
                .uri(ADMIN_USER_ENDPOINT + "/" + userId[0])
                .header("Authorization", "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus().isNoContent();
    }
}