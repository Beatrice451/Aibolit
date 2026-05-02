package org.beatrice.diploma_new_pharmacy.product.integration;

import org.beatrice.diploma_new_pharmacy.config.BaseIntegrationTest;
import org.beatrice.diploma_new_pharmacy.domain.auth.dto.AuthRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.AddReviewRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Category;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Product;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.CategoryRepository;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.ProductRepository;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Product Integration Tests")
class ProductTest extends BaseIntegrationTest {

    private static final String PRODUCTS_ENDPOINT = "/api/products";
    private static final String MEDICINES_ENDPOINT = "/api/products/medicines";
    private static final String CATEGORIES_ENDPOINT = "/api/categories";
    private static final String TEST_EMAIL = "testuser@example.com";
    private static final String TEST_PASSWORD = "SecurePassword123!";

    @LocalServerPort
    private int port;

    private RestTestClient restClient;
    private String validAccessToken;
    private int testCategoryId;
    private int testProductId;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

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

        Category testCategory = Category.builder()
                .name("Test Category")
                .build();
        testCategory = categoryRepository.save(testCategory);
        testCategoryId = testCategory.getId();

        Product testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .manufacturer("Test Manufacturer")
                .price(BigDecimal.valueOf(100.00))
                .category(testCategory)
                .build();
        testProduct = productRepository.save(testProduct);
        testProductId = testProduct.getId();

        AuthRequest loginRequest = new AuthRequest(TEST_EMAIL, TEST_PASSWORD);

        restClient = RestTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        validAccessToken = restClient.post()
                .uri("/api/auth/login")
                .body(loginRequest)
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
        if (end == -1) end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    @Test
    @DisplayName("TC-18: Should return paginated list of products")
    void shouldReturnPaginatedListOfProducts() {
        restClient.get()
                .uri(PRODUCTS_ENDPOINT + "?page=0&size=10")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-19: Should return product by ID")
    void shouldReturnProductById() {
        restClient.get()
                .uri(PRODUCTS_ENDPOINT + "/" + testProductId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-20: Should return 404 for non-existent product")
    void shouldReturn404ForNonExistentProduct() {
        restClient.get()
                .uri(PRODUCTS_ENDPOINT + "/999999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("TC-21: Should return list of medicines")
    void shouldReturnListOfMedicines() {
        restClient.get()
                .uri(MEDICINES_ENDPOINT)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-22: Should filter products by category")
    void shouldFilterProductsByCategory() {
        restClient.get()
                .uri(PRODUCTS_ENDPOINT + "?categoryId=" + testCategoryId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-23: Should search products by name")
    void shouldSearchProductsByName() {
        restClient.get()
                .uri(PRODUCTS_ENDPOINT + "?search=Test")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-24: Should return correct pagination")
    void shouldReturnCorrectPagination() {
        restClient.get()
                .uri(PRODUCTS_ENDPOINT + "?page=0&size=5")
                .exchange()
                .expectStatus().isOk();
    }

@Test
    @DisplayName("TC-25: Should add review to product when authenticated")
    void shouldAddReviewToProductWhenAuthenticated() {
        AddReviewRequest review = new AddReviewRequest((short) 5, "Good product");

        restClient.post()
                .uri(PRODUCTS_ENDPOINT + "/" + testProductId + "/reviews")
                .header("Authorization", "Bearer " + validAccessToken)
                .body(review)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    @DisplayName("TC-26: Should reject review without authentication")
    void shouldRejectReviewWithoutAuthentication() {
        AddReviewRequest review = new AddReviewRequest((short) 5, "Good product");

        restClient.post()
                .uri(PRODUCTS_ENDPOINT + "/" + testProductId + "/reviews")
                .body(review)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("TC-27: Should return product reviews")
    void shouldReturnProductReviews() {
        restClient.get()
                .uri(PRODUCTS_ENDPOINT + "/" + testProductId + "/reviews?page=0&size=10")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-28: Should delete own review")
    void shouldDeleteOwnReview() {
        // First, add a review
        AddReviewRequest review = new AddReviewRequest((short) 5, "Great product");
        
        final int[] reviewId = new int[1];
        
        restClient.post()
                .uri(PRODUCTS_ENDPOINT + "/" + testProductId + "/reviews")
                .header("Authorization", "Bearer " + validAccessToken)
                .body(review)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    if (body != null) {
                        // Extract reviewId from response
                        int start = body.indexOf("\"reviewId\":");
                        if (start != -1) {
                            start += 11;
                            int end = body.indexOf(",", start);
                            if (end == -1) end = body.indexOf("}", start);
                            reviewId[0] = Integer.parseInt(body.substring(start, end).trim());
                        }
                    }
                });

        // Then delete it
        restClient.delete()
                .uri("/api/reviews/" + reviewId[0])
                .header("Authorization", "Bearer " + validAccessToken)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("TC-29: Should reject deleting someone else's review")
    void shouldRejectDeletingOthersReview() {
        // Create another user
        User anotherUser = TestDataFactory.createUser(
                "another@example.com",
                "+79009999999",
                passwordEncoder.encode(TEST_PASSWORD)
        );
        userRepository.save(anotherUser);

        // Login as another user and create a review
        String anotherToken = restClient.post()
                .uri("/api/auth/login")
                .body(new AuthRequest("another@example.com", TEST_PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody();
        
        anotherToken = extractAccessToken(anotherToken);

        AddReviewRequest review = new AddReviewRequest((short) 4, "Good product");
        
        final int[] reviewId = new int[1];
        
        restClient.post()
                .uri(PRODUCTS_ENDPOINT + "/" + testProductId + "/reviews")
                .header("Authorization", "Bearer " + anotherToken)
                .body(review)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    if (body != null) {
                        int start = body.indexOf("\"reviewId\":");
                        if (start != -1) {
                            start += 11;
                            int end = body.indexOf(",", start);
                            if (end == -1) end = body.indexOf("}", start);
                            reviewId[0] = Integer.parseInt(body.substring(start, end).trim());
                        }
                    }
                });

        // Try to delete another user's review with original user token
        restClient.delete()
                .uri("/api/reviews/" + reviewId[0])
                .header("Authorization", "Bearer " + validAccessToken)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("TC-42: Should return list of categories")
    void shouldReturnListOfCategories() {
        restClient.get()
                .uri(CATEGORIES_ENDPOINT)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-43: Should return category by ID")
    void shouldReturnCategoryById() {
        restClient.get()
                .uri(CATEGORIES_ENDPOINT + "/" + testCategoryId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-44: Should return 404 for non-existent category")
    void shouldReturn404ForNonExistentCategory() {
        restClient.get()
                .uri(CATEGORIES_ENDPOINT + "/999999")
                .exchange()
                .expectStatus().isNotFound();
    }
}