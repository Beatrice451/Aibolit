# Authentication & Authorization Integration Tests

## Overview
This test suite provides comprehensive coverage of the authentication and authorization module using **BDD (Behavior-Driven Development)** style with **Given-When-Then** structure.

## Test Coverage

### Total: 12 Test Cases

| Test Class | Test Cases | Description |
|------------|-----------|-------------|
| `RegistrationTest` | 5 | User registration scenarios |
| `LoginTest` | 4 | User login scenarios |
| `TokenRefreshTest` | 2 | JWT token refresh and rotation |
| `LogoutTest` | 1 | User logout and token revocation |
| `AuthorizationTest` | 3 | Access control and role-based authorization |

## Technology Stack

- **JUnit 5** - Testing framework
- **Spring Boot Test** - Integration testing support
- **Testcontainers** - PostgreSQL 16 in Docker for isolated tests
- **RestTestClient** - HTTP client for API testing
- **AssertJ** - Fluent assertions

## Test Structure

```
backend/src/test/java/org/beatrice/diploma_new_pharmacy/
├── auth/integration/
│   ├── RegistrationTest.java      # TC-1 to TC-5
│   ├── LoginTest.java              # TC-6 to TC-9
│   ├── TokenRefreshTest.java      # TC-10 to TC-11
│   ├── LogoutTest.java             # TC-12
│   └── AuthorizationTest.java     # TC-13 to TC-14
├── config/
│   └── BaseIntegrationTest.java   # Base class with Testcontainers setup
└── util/
    └── TestDataFactory.java        # Test data generation utilities
```

## Running Tests

### Run all authentication tests:
```bash
./gradlew test --tests "org.beatrice.diploma_new_pharmacy.auth.integration.*"
```

### Run specific test class:
```bash
./gradlew test --tests "org.beatrice.diploma_new_pharmacy.auth.integration.LoginTest"
```

### Run single test case:
```bash
./gradlew test --tests "org.beatrice.diploma_new_pharmacy.auth.integration.LoginTest.shouldReturnTokens_whenCredentialsAreValid"
```

## Test Details

### RegistrationTest (5 tests)
- **TC-1**: Register user with valid data
- **TC-2**: Reject registration when email already exists (409 Conflict)
- **TC-3**: Reject registration when phone already exists (409 Conflict)
- **TC-4**: Reject registration with invalid email format (400 Bad Request)
- **TC-5**: Reject registration with short password (400 Bad Request)

### LoginTest (4 tests)
- **TC-6**: Return access & refresh tokens when credentials are valid
- **TC-7**: Reject login with incorrect password (401 Unauthorized)
- **TC-8**: Reject login with non-existent email (401 Unauthorized)
- **TC-9**: Reject login when required fields are missing (400 Bad Request)

### TokenRefreshTest (2 tests)
- **TC-10**: Return new tokens when refresh token is valid (token rotation verified)
- **TC-11**: Reject refresh when token is expired or invalid (401 Unauthorized)

### LogoutTest (1 test)
- **TC-12**: Successfully logout, revoke refresh token, and delete cookie

### AuthorizationTest (3 tests)
- **TC-13**: Reject access to protected endpoint without token (401 Unauthorized)
- **TC-14**: Reject access to admin endpoint without ADMIN role (403 Forbidden)
- **TC-14-BONUS**: Allow access to admin endpoint with ADMIN role (200 OK)

## Key Features

### 1. **Testcontainers Integration**
- Real PostgreSQL 16 database in Docker
- Automatic container lifecycle management
- Container reuse for performance optimization

### 2. **BDD Style**
- Clear Given-When-Then structure
- Descriptive test names: `shouldExpectedBehavior_whenCondition`
- Readable assertions with AssertJ

### 3. **Test Isolation**
- Each test starts with clean database state
- `@BeforeEach` setup ensures independence
- No test pollution or side effects

### 4. **Token Rotation Testing**
- Verifies old refresh token is revoked after refresh
- Ensures new token is created and valid
- Checks database state consistency

### 5. **Security Verification**
- Password hashing validation
- JWT token structure verification
- HttpOnly cookie validation
- Role-based access control testing

## Example Test (BDD Style)

```java
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
```

## Configuration

### Test Profile (`application-test.yaml`)
- PostgreSQL configured via Testcontainers
- Flyway migrations enabled
- Email sending disabled
- Debug logging for application code
- Test JWT secret

## Notes

- **Docker Required**: Testcontainers needs Docker to run PostgreSQL
- **First Run**: Initial test run downloads PostgreSQL image (~100MB)
- **Performance**: Container reuse speeds up subsequent test runs
- **LSP Errors**: IDE may show errors, but tests compile and run correctly

## Future Enhancements

- [ ] Add performance tests for concurrent logins
- [ ] Test JWT expiration edge cases
- [ ] Add tests for password reset flow
- [ ] Test rate limiting on login attempts
- [ ] Add tests for refresh token family invalidation
