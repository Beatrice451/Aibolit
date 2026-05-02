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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Login Integration Tests")
class LoginTest extends BaseIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String TEST_EMAIL = "testuser@example.com";
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

        // Given: USER role exists
        Role userRole = TestDataFactory.createRole("USER");
        roleRepository.save(userRole);

        // Given: Test user exists with known credentials
        User testUser = TestDataFactory.createUser(
                TEST_EMAIL,
                "+79001234567",
                passwordEncoder.encode(TEST_PASSWORD)
        );
        userRepository.save(testUser);

        // Given: REST client configured
        restClient = RestTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("TC-6: Should return tokens when credentials are valid")
    void shouldReturnTokens_whenCredentialsAreValid() {
        // Given: Valid login credentials
        AuthRequest request = TestDataFactory.createValidAuthRequest(TEST_EMAIL, TEST_PASSWORD);

        // When: User attempts to login
        restClient.post()
                .uri(LOGIN_ENDPOINT)
                .body(request)
                .exchange()
                // Then: Login succeeds with 200 OK
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    // Then: Access token is present in response body
                    String responseBody = result.getResponseBody();
                    assertThat(responseBody).isNotNull();
                    assertThat(responseBody).contains("accessToken");

                    // Then: Refresh token is set in cookie
                    var cookies = result.getResponseHeaders().get("Set-Cookie");
                    assertThat(cookies).isNotNull();
                    assertThat(cookies).anyMatch(cookie -> cookie.contains("refreshToken"));
                    assertThat(cookies).anyMatch(cookie -> cookie.contains("HttpOnly"));
                });
    }

    @Test
    @DisplayName("TC-7: Should reject login when password is incorrect")
    void shouldRejectLogin_whenPasswordIsIncorrect() {
        // Given: Login request with wrong password
        AuthRequest request = TestDataFactory.createValidAuthRequest(TEST_EMAIL, "WrongPassword123!");

        // When: User attempts to login with wrong password
        restClient.post()
                .uri(LOGIN_ENDPOINT)
                .body(request)
                .exchange()
                // Then: Login fails with 401 Unauthorized
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("TC-8: Should reject login when email does not exist")
    void shouldRejectLogin_whenEmailDoesNotExist() {
        // Given: Login request with non-existent email
        AuthRequest request = TestDataFactory.createValidAuthRequest("nonexistent@example.com", TEST_PASSWORD);

        // When: User attempts to login with non-existent email
        restClient.post()
                .uri(LOGIN_ENDPOINT)
                .body(request)
                .exchange()
                // Then: Login fails with 401 Unauthorized
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("TC-9: Should reject login when required fields are missing")
    void shouldRejectLogin_whenRequiredFieldsAreMissing() {
        // Given: Login request with null email
        AuthRequest requestWithNullEmail = new AuthRequest(null, TEST_PASSWORD);

        // When: User attempts to login without email
        restClient.post()
                .uri(LOGIN_ENDPOINT)
                .body(requestWithNullEmail)
                .exchange()
                // Then: Login fails with 400 Bad Request (validation error)
                .expectStatus().isBadRequest();

        // Given: Login request with null password
        AuthRequest requestWithNullPassword = new AuthRequest(TEST_EMAIL, null);

        // When: User attempts to login without password
        restClient.post()
                .uri(LOGIN_ENDPOINT)
                .body(requestWithNullPassword)
                .exchange()
                // Then: Login fails with 400 Bad Request (validation error)
                .expectStatus().isBadRequest();
    }
}
