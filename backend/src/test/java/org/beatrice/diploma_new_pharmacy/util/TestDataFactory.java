package org.beatrice.diploma_new_pharmacy.util;

import org.beatrice.diploma_new_pharmacy.domain.auth.dto.AuthRequest;
import org.beatrice.diploma_new_pharmacy.domain.auth.dto.UserRegistrationRequest;
import org.beatrice.diploma_new_pharmacy.domain.user.model.Role;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Factory for creating test data objects.
 * Uses atomic counters to ensure unique values across tests.
 */
public class TestDataFactory {

    private static final AtomicInteger userCounter = new AtomicInteger(0);

    /**
     * Creates a valid UserRegistrationRequest with unique email and phone.
     */
    public static UserRegistrationRequest createValidRegistrationRequest() {
        int id = userCounter.incrementAndGet();
        return new UserRegistrationRequest(
                "test" + id + "@example.com",
                "+7900000" + String.format("%04d", id),
                "SecurePassword123!",
                "test",
                "test"
        );
    }

    /**
     * Creates a UserRegistrationRequest with custom values.
     */
    public static UserRegistrationRequest createRegistrationRequest(
            String email,
            String phone,
            String password,
            String firstName,
            String lastName
    ) {

        return new UserRegistrationRequest(email, phone, password, firstName, lastName);
    }

    /**
     * Creates a valid AuthRequest (login credentials).
     */
    public static AuthRequest createValidAuthRequest(String email, String password) {
        return new AuthRequest(email, password);
    }

    /**
     * Creates a User entity for database seeding.
     */
    public static User createUser(String email, String phone, String passwordHash) {
        User user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordHash);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setIsDeleted(false);
        user.setUserRoles(new HashSet<>());
        return user;
    }

    /**
     * Creates a Role entity.
     */
    public static Role createRole(String roleName) {
        Role role = new Role();
        role.setRoleName(roleName);
        return role;
    }

    /**
     * Creates an invalid email for validation testing.
     */
    public static String createInvalidEmail() {
        return "invalid-email-format";
    }

    /**
     * Creates a short password for validation testing.
     */
    public static String createShortPassword() {
        return "123";
    }
}
