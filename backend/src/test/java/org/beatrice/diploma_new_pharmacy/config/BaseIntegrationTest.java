package org.beatrice.diploma_new_pharmacy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using Testcontainers with PostgreSQL.
 * All integration tests should extend this class to get:
 * - Real PostgreSQL database in Docker container
 * - Automatic container lifecycle management
 * - Test profile activation
 * - Web environment with random port
 * - Database cleanup utility
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    protected static final PostgreSQLContainer<?> postgresContainer;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    static {
        postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("pharmacy_test")
                .withUsername("test")
                .withPassword("test");
        postgresContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    /**
     * Cleans all test data from the database.
     * Uses TRUNCATE with CASCADE to handle foreign key constraints.
     * Resets sequences with RESTART IDENTITY.
     * 
     * Call this method in @BeforeEach to ensure clean state for each test.
     */
    protected void cleanDatabase() {
        jdbcTemplate.execute("""
            TRUNCATE TABLE 
                pharmacy.cart_items,
                pharmacy.carts,
                pharmacy.order_items,
                pharmacy.orders,
                pharmacy.order_owners,
                pharmacy.reviews,
                pharmacy.products,
                pharmacy.medicines,
                pharmacy.medicine_active_substances,
                pharmacy.medicine_symptoms,
                pharmacy.categories,
                pharmacy.user_roles,
                pharmacy.users,
                pharmacy.roles,
                pharmacy.refresh_tokens
            RESTART IDENTITY CASCADE
        """);
    }
}
