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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Profile Integration Tests")
class UserTest extends BaseIntegrationTest {

    private static final String USER_ENDPOINT = "/api/users";
    private static final String AUTH_ENDPOINT = "/api/auth/login";
    private static final String TEST_EMAIL = "testuser@example.com";
    private static final String TEST_PASSWORD = "SecurePassword123!";

    @LocalServerPort
    private int port;

    private RestTestClient restClient;
    private String validAccessToken;

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
        roleRepository.save(userRole);

        User testUser = TestDataFactory.createUser(
                TEST_EMAIL,
                "+79001234567",
                passwordEncoder.encode(TEST_PASSWORD)
        );
        userRepository.save(testUser);

        restClient = RestTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        validAccessToken = restClient.post()
                .uri(AUTH_ENDPOINT)
                .body(new AuthRequest(TEST_EMAIL, TEST_PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody();

        validAccessToken = extractAccessToken(validAccessToken);
    }

    private String extractAccessToken(String json) {
        if (json == null) return null;
        int start = json.indexOf("\"accessToken\":\"");
        if (start == -1) start = json.indexOf("\"accessToken\" : \"");
        if (start == -1) return json;
        start += 15;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    @Test
    @DisplayName("TC-49: Should get current user profile")
    void shouldGetCurrentUserProfile() {
        restClient.get()
                .uri(USER_ENDPOINT + "/whoami")
                .header("Authorization", "Bearer " + validAccessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).contains(TEST_EMAIL);
                    assertThat(body).contains("\"+79001234567\"");
                });
    }

    @Test
    @DisplayName("TC-50: Should return 418 when not authenticated")
    void shouldReturn418WhenNotAuthenticated() {
        restClient.get()
                .uri(USER_ENDPOINT + "/whoami")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("TC-51: Should delete own account")
    void shouldDeleteOwnAccount() {
        restClient.delete()
                .uri(USER_ENDPOINT + "/me")
                .header("Authorization", "Bearer " + validAccessToken)
                .exchange()
                .expectStatus().isOk();

        restClient.get()
                .uri(USER_ENDPOINT + "/whoami")
                .header("Authorization", "Bearer " + validAccessToken)
                .exchange()
                .expectStatus().is4xxClientError();
    }
}