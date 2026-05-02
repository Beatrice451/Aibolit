package org.beatrice.diploma_new_pharmacy.auth.integration;

import org.beatrice.diploma_new_pharmacy.config.BaseIntegrationTest;
import org.beatrice.diploma_new_pharmacy.domain.auth.dto.AuthRequest;
import org.beatrice.diploma_new_pharmacy.domain.auth.model.RefreshToken;
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
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Token Refresh Integration Tests")
class TokenRefreshTest extends BaseIntegrationTest {

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
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
    @DisplayName("TC-10: Should return new tokens when refresh token is valid")
    void shouldReturnNewTokens_whenRefreshTokenIsValid() {
        // Given: User is logged in and has valid refresh token
        AuthRequest loginRequest = TestDataFactory.createValidAuthRequest(TEST_EMAIL, TEST_PASSWORD);
        
        final String[] oldAccessToken = new String[1];
        final String[] refreshTokenCookie = new String[1];
        final String[] oldRefreshTokenValue = new String[1];
        
        restClient.post()
                .uri(LOGIN_ENDPOINT)
                .body(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    oldAccessToken[0] = extractAccessToken(result.getResponseBody());
                    refreshTokenCookie[0] = extractRefreshTokenFromCookie(result.getResponseHeaders().get("Set-Cookie"));
                    oldRefreshTokenValue[0] = extractRefreshTokenValue(refreshTokenCookie[0]);
                });

        // Given: Old refresh token exists in database
        long oldTokenCountBefore = refreshTokenRepository.count();
        assertThat(refreshTokenRepository.findByToken(oldRefreshTokenValue[0])).isPresent();

        // When: User requests token refresh
        restClient.post()
                .uri(REFRESH_ENDPOINT)
                .header(HttpHeaders.COOKIE, refreshTokenCookie[0])
                .exchange()
                // Then: Refresh succeeds with 200 OK
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    // Then: New access token is returned
                    String newAccessToken = extractAccessToken(result.getResponseBody());
                    assertThat(newAccessToken).isNotNull();
                    assertThat(newAccessToken).isNotEqualTo(oldAccessToken[0]);

                    // Then: New refresh token is set in cookie
                    String newRefreshTokenCookie = extractRefreshTokenFromCookie(result.getResponseHeaders().get("Set-Cookie"));
                    String newRefreshTokenValue = extractRefreshTokenValue(newRefreshTokenCookie);
                    assertThat(newRefreshTokenValue).isNotNull();
                    assertThat(newRefreshTokenValue).isNotEqualTo(oldRefreshTokenValue[0]);

                    // Then: Token rotation occurred - old token is revoked, new token exists
                    assertThat(refreshTokenRepository.findByToken(oldRefreshTokenValue[0]))
                            .isPresent()
                            .hasValueSatisfying(token -> assertThat(token.getRevoked()).isTrue());

                    assertThat(refreshTokenRepository.findByToken(newRefreshTokenValue))
                            .isPresent()
                            .hasValueSatisfying(token -> assertThat(token.getRevoked()).isFalse());

                    // Then: Total token count increased by 1 (old revoked, new created)
                    assertThat(refreshTokenRepository.count()).isEqualTo(oldTokenCountBefore + 1);
                });
    }

    @Test
    @DisplayName("TC-11: Should reject refresh when token is expired or invalid")
    void shouldRejectRefresh_whenTokenIsExpiredOrInvalid() {
        // Given: Invalid refresh token
        String invalidRefreshToken = "refreshToken=invalid-token-value; Path=/; HttpOnly";

        // When: User attempts to refresh with invalid token
        restClient.post()
                .uri(REFRESH_ENDPOINT)
                .header(HttpHeaders.COOKIE, invalidRefreshToken)
                .exchange()
                // Then: Refresh fails with 401 Unauthorized
                .expectStatus().isUnauthorized();

        // Given: No refresh token provided
        // When: User attempts to refresh without token
        restClient.post()
                .uri(REFRESH_ENDPOINT)
                .exchange()
                // Then: Refresh fails with 400 Bad Request or 401 Unauthorized
                .expectStatus().is4xxClientError();
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
