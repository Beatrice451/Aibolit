package org.beatrice.diploma_new_pharmacy.auth.integration;

import org.beatrice.diploma_new_pharmacy.config.BaseIntegrationTest;
import org.beatrice.diploma_new_pharmacy.domain.auth.dto.AuthRequest;
import org.beatrice.diploma_new_pharmacy.domain.auth.repository.RefreshTokenRepository;
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
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Logout Integration Tests")
class LogoutTest extends BaseIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String LOGOUT_ENDPOINT = "/api/auth/logout";
    private static final String REFRESH_ENDPOINT = "/api/auth/refresh";
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
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        // Given: USER role exists
        Role userRole = TestDataFactory.createRole("USER");
        roleRepository.save(userRole);

        // Given: Test user exists
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
    @DisplayName("TC-12: Should successfully logout and revoke refresh token")
    void shouldSuccessfullyLogout_andRevokeRefreshToken() {
        // Given: User is logged in with valid refresh token
        AuthRequest loginRequest = TestDataFactory.createValidAuthRequest(TEST_EMAIL, TEST_PASSWORD);

        AtomicReference<String> refreshTokenCookie = new AtomicReference<>();
        AtomicReference<String> refreshTokenValue = new AtomicReference<>();

        restClient.post()
                .uri(LOGIN_ENDPOINT)
                .body(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    List<String> cookies = result.getResponseHeaders().get(HttpHeaders.SET_COOKIE);
                    assertThat(cookies).isNotNull();

                    String cookie = extractRefreshTokenFromCookie(cookies);
                    refreshTokenCookie.set(cookie);
                    refreshTokenValue.set(extractRefreshTokenValue(cookie));
                });

        // Given: Refresh token exists and is not revoked
        assertThat(refreshTokenRepository.findByToken(refreshTokenValue.get()))
                .isPresent()
                .hasValueSatisfying(token -> assertThat(token.getRevoked()).isFalse());

        // When: User logs out
        EntityExchangeResult<Void> logoutResult = restClient.post()
                .uri(LOGOUT_ENDPOINT)
                .header(HttpHeaders.COOKIE, refreshTokenCookie.get())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .isEmpty();

        // Then: Refresh token cookie is deleted (Max-Age=0)
        List<String> logoutCookies = logoutResult.getResponseHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(logoutCookies).isNotNull();
        assertThat(logoutCookies).anyMatch(cookie ->
                                                   cookie.contains("refreshToken=") && cookie.contains("Max-Age=0")
        );

        // Then: Refresh token is revoked in database
        assertThat(refreshTokenRepository.findByToken(refreshTokenValue.get()))
                .isPresent()
                .hasValueSatisfying(token -> assertThat(token.getRevoked()).isTrue());

        // Then: Cannot use revoked token to refresh
        restClient.post()
                .uri(REFRESH_ENDPOINT)
                .header(HttpHeaders.COOKIE, refreshTokenCookie.get())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Helper method to extract refresh token cookie from Set-Cookie headers.
     */
    private String extractRefreshTokenFromCookie(List<String> cookies) {
        if (cookies == null) return null;
        return cookies.stream()
                .filter(cookie -> cookie.contains("refreshToken="))
                .findFirst()
                .orElse(null);
    }

    /**
     * Helper method to extract refresh token value from cookie string.
     */
    private String extractRefreshTokenValue(String cookie) {
        if (cookie == null) return null;
        Pattern pattern = Pattern.compile("refreshToken=([^;]+)");
        Matcher matcher = pattern.matcher(cookie);
        return matcher.find() ? matcher.group(1) : null;
    }
}
