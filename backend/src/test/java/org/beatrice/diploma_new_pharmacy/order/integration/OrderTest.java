package org.beatrice.diploma_new_pharmacy.order.integration;

import org.beatrice.diploma_new_pharmacy.config.BaseIntegrationTest;
import org.beatrice.diploma_new_pharmacy.domain.auth.dto.AuthRequest;
import org.beatrice.diploma_new_pharmacy.domain.cart.dto.request.AddProductToCartRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.request.CreateOrderRequest;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Pharmacy;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository.PharmacyRepository;
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

@DisplayName("Order Integration Tests")
class OrderTest extends BaseIntegrationTest {

    private static final String ORDERS_ENDPOINT = "/api/orders";
    private static final String CART_ENDPOINT = "/api/cart";
    private static final String TEST_EMAIL = "testuser@example.com";
    private static final String TEST_PASSWORD = "SecurePassword123!";
    private static final String GUEST_COOKIE_NAME = "guestCartId";

    @LocalServerPort
    private int port;

    private RestTestClient restClient;
    private String validAccessToken;
    private int testProductId;
    private int testPharmacyId;

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

    @Autowired
    private PharmacyRepository pharmacyRepository;

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

        Pharmacy testPharmacy = new Pharmacy("Test Pharmacy", "Test Address", "+79001111111");
        testPharmacy = pharmacyRepository.save(testPharmacy);
        testPharmacyId = testPharmacy.getId();

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
    @DisplayName("TC-39: Should create order for authenticated user")
    void shouldCreateOrderForAuthenticatedUser() {
        // Add product to cart
        AddProductToCartRequest cartRequest = new AddProductToCartRequest(testProductId, (short) 2);
        restClient.post()
                .uri(CART_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .body(cartRequest)
                .exchange()
                .expectStatus().isOk();

        // Create order
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testPharmacyId,
                "+79001234567",
                TEST_EMAIL,
                "Test",
                "User"
        );

        restClient.post()
                .uri(ORDERS_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .body(orderRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).contains("id");
                    assertThat(body).contains("pickupCode");
                });
    }

    @Test
    @DisplayName("TC-40: Should create order for guest")
    void shouldCreateOrderForGuest() {
        // Add product to cart as guest
        AddProductToCartRequest cartRequest = new AddProductToCartRequest(testProductId, (short) 1);
        
        final String[] guestCookie = new String[1];
        
        restClient.post()
                .uri(CART_ENDPOINT)
                .body(cartRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    var cookies = result.getResponseHeaders().get("Set-Cookie");
                    if (cookies != null && !cookies.isEmpty()) {
                        guestCookie[0] = cookies.get(0);
                    }
                });

        // Create order as guest
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testPharmacyId,
                "+79009999999",
                "guest@example.com",
                "Guest",
                "User"
        );

        restClient.post()
                .uri(ORDERS_ENDPOINT)
                .header("Cookie", guestCookie[0])
                .body(orderRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).contains("id");
                });
    }

    @Test
    @DisplayName("TC-41: Should reject order with empty cart")
    void shouldRejectOrderWithEmptyCart() {
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testPharmacyId,
                "+79001234567",
                TEST_EMAIL,
                "Test",
                "User"
        );

        restClient.post()
                .uri(ORDERS_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .body(orderRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("TC-42: Should get orders for user")
    void shouldGetOrdersForUser() {
        // Add product to cart and create order first
        AddProductToCartRequest cartRequest = new AddProductToCartRequest(testProductId, (short) 1);
        restClient.post()
                .uri(CART_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .body(cartRequest)
                .exchange()
                .expectStatus().isOk();

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testPharmacyId,
                "+79001234567",
                TEST_EMAIL,
                "Test",
                "User"
        );

        restClient.post()
                .uri(ORDERS_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .body(orderRequest)
                .exchange()
                .expectStatus().isOk();

        // Get orders
        restClient.get()
                .uri(ORDERS_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).contains("id");
                });
    }

    @Test
    @DisplayName("TC-43: Should get order by ID")
    void shouldGetOrderById() {
        // Create order first
        AddProductToCartRequest cartRequest = new AddProductToCartRequest(testProductId, (short) 1);
        restClient.post()
                .uri(CART_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .body(cartRequest)
                .exchange()
                .expectStatus().isOk();

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testPharmacyId,
                "+79001234567",
                TEST_EMAIL,
                "Test",
                "User"
        );

        final int[] orderId = new int[1];

        restClient.post()
                .uri(ORDERS_ENDPOINT)
                .header("Authorization", "Bearer " + validAccessToken)
                .body(orderRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    int start = body.indexOf("\"id\":");
                    if (start != -1) {
                        start += 5;
                        int end = body.indexOf(",", start);
                        if (end == -1) end = body.indexOf("}", start);
                        orderId[0] = Integer.parseInt(body.substring(start, end).trim());
                    }
                });

        // Get order by ID
        restClient.get()
                .uri(ORDERS_ENDPOINT + "/" + orderId[0])
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).contains("id");
                });
    }
}
