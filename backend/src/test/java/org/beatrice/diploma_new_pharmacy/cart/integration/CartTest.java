package org.beatrice.diploma_new_pharmacy.cart.integration;

import org.beatrice.diploma_new_pharmacy.config.BaseIntegrationTest;
import org.beatrice.diploma_new_pharmacy.domain.auth.dto.AuthRequest;
import org.beatrice.diploma_new_pharmacy.domain.cart.dto.request.AddProductToCartRequest;
import org.beatrice.diploma_new_pharmacy.domain.cart.dto.request.SetProductInCartRequest;
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

@DisplayName("Cart Integration Tests")
class CartTest extends BaseIntegrationTest {

    private static final String CART_ENDPOINT = "/api/cart";
    private static final String TEST_EMAIL = "testuser@example.com";
    private static final String TEST_PASSWORD = "SecurePassword123!";
    private static final String GUEST_COOKIE_NAME = "guestCartId";

    @LocalServerPort
    private int port;

    private RestTestClient restClient;
    private String validAccessToken;
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
        return json.substring(start, end);
    }

    @Test
    @DisplayName("TC-30: Should get cart for guest")
    void shouldGetCartForGuest() {
        restClient.get()
                .uri(CART_ENDPOINT)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-31: Should get cart for authenticated user")
    void shouldGetCartForAuthenticatedUser() {
        restClient.get()
                .uri(CART_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-32: Should add product to guest cart")
    void shouldAddProductToGuestCart() {
        AddProductToCartRequest request = new AddProductToCartRequest(testProductId, (short) 2);
        
        restClient.post()
                .uri(CART_ENDPOINT)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).isNotNull();
                    
                    // Verify guest cookie was created
                    var cookies = result.getResponseHeaders().get("Set-Cookie");
                    assertThat(cookies).isNotNull();
                    assertThat(cookies.toString()).contains(GUEST_COOKIE_NAME);
                });
    }

    @Test
    @DisplayName("TC-33: Should add product to user cart")
    void shouldAddProductToUserCart() {
        AddProductToCartRequest request = new AddProductToCartRequest(testProductId, (short) 3);
        
        restClient.post()
                .uri(CART_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .body(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-34: Should return 404 when adding non-existent product")
    void shouldReturn404WhenAddingNonExistentProduct() {
        AddProductToCartRequest request = new AddProductToCartRequest(999999, (short) 1);
        
        restClient.post()
                .uri(CART_ENDPOINT)
                .body(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("TC-35: Should update product quantity in cart")
    void shouldUpdateProductQuantityInCart() {
        // First add product to cart
        AddProductToCartRequest addRequest = new AddProductToCartRequest(testProductId, (short) 1);
        restClient.post()
                .uri(CART_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .body(addRequest)
                .exchange()
                .expectStatus().isOk();

        // Then update quantity
        SetProductInCartRequest updateRequest = new SetProductInCartRequest((short) 5);
        
        restClient.put()
                .uri(CART_ENDPOINT + "/" + testProductId)
                .header("Authorization", "Bearer " + validAccessToken)
                .body(updateRequest)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-36: Should delete guest cart")
    void shouldDeleteGuestCart() {
        // First add product to cart
        AddProductToCartRequest request = new AddProductToCartRequest(testProductId, (short) 1);
        
        final String[] guestCookie = new String[1];
        
        restClient.post()
                .uri(CART_ENDPOINT)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    var cookies = result.getResponseHeaders().get("Set-Cookie");
                    if (cookies != null && !cookies.isEmpty()) {
                        guestCookie[0] = cookies.get(0);
                    }
                });

        // Then delete cart
        restClient.delete()
                .uri(CART_ENDPOINT)
                .header("Cookie", guestCookie[0])
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-37: Should delete user cart")
    void shouldDeleteUserCart() {
        // First add product to cart
        AddProductToCartRequest request = new AddProductToCartRequest(testProductId, (short) 1);
        
        restClient.post()
                .uri(CART_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .body(request)
                .exchange()
                .expectStatus().isOk();

        // Then delete cart
        restClient.delete()
                .uri(CART_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("TC-38: Should create guest cookie on first request")
    void shouldCreateGuestCookieOnFirstRequest() {
        restClient.get()
                .uri(CART_ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    var cookies = result.getResponseHeaders().get("Set-Cookie");
                    assertThat(cookies).isNotNull();
                    assertThat(cookies.toString()).contains(GUEST_COOKIE_NAME);
                });
    }
}