package org.beatrice.diploma_new_pharmacy.order.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.beatrice.diploma_new_pharmacy.config.BaseIntegrationTest;
import org.beatrice.diploma_new_pharmacy.domain.auth.dto.AuthRequest;
import org.beatrice.diploma_new_pharmacy.domain.cart.dto.request.AddProductToCartRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.request.CreateOrderRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.request.UpdateOrderStatusRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.request.VerifyPickupCodeRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderStatus;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Pharmacy;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Stock;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Warehouse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository.PharmacyRepository;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository.StockRepository;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository.WarehouseRepository;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Admin Order Integration Tests")
class AdminOrderTest extends BaseIntegrationTest {

    private static final String ADMIN_ORDER_ENDPOINT = "/api/admin/order";
    private static final String ORDERS_ENDPOINT = "/api/orders";
    private static final String CART_ENDPOINT = "/api/cart";
    private static final String AUTH_ENDPOINT = "/api/auth/login";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String USER_EMAIL = "user@example.com";
    private static final String TEST_PASSWORD = "SecurePassword123!";

    @LocalServerPort
    private int port;

    private RestTestClient restClient;
    private String adminAccessToken;
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

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        Role userRole = TestDataFactory.createRole("USER");
        Role adminRole = TestDataFactory.createRole("ADMIN");
        roleRepository.save(userRole);
        roleRepository.save(adminRole);

        User regularUser = TestDataFactory.createUser(
                USER_EMAIL,
                "+79001234567",
                passwordEncoder.encode(TEST_PASSWORD)
        );
        regularUser.setUserRoles(Set.of(userRole));
        userRepository.save(regularUser);

        User adminUser = TestDataFactory.createUser(
                ADMIN_EMAIL,
                "+79002222222",
                passwordEncoder.encode(TEST_PASSWORD)
        );
        adminUser.setUserRoles(Set.of(userRole, adminRole));
        userRepository.save(adminUser);

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

        Warehouse warehouse = new Warehouse();
        warehouse.setName("Test Warehouse");
        warehouse.setAddress("Test Address");
        warehouse.setPharmacy(testPharmacy);
        warehouse = warehouseRepository.save(warehouse);

        Stock stock = new Stock();
        stock.getId().setProductId(testProductId);
        stock.getId().setWarehouseId(warehouse.getId());
        stock.setProduct(testProduct);
        stock.setWarehouse(warehouse);
        stock.setQuantity(100);
        stockRepository.save(stock);

        restClient = RestTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        adminAccessToken = restClient.post()
                .uri(AUTH_ENDPOINT)
                .body(new AuthRequest(ADMIN_EMAIL, TEST_PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody();

        adminAccessToken = extractAccessToken(adminAccessToken);
    }

    private String extractAccessToken(String json) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractPickupCode(String json) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"pickupCode\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private Integer extractOrderId(String json) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    @Test
    @DisplayName("TC-44: Admin should verify valid pickup code")
    void shouldVerifyValidPickupCode() throws Exception {
        // Login as regular user and add product to cart with auth
        String userAccessToken = restClient.post()
                .uri(AUTH_ENDPOINT)
                .body(new AuthRequest(USER_EMAIL, TEST_PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody();
        userAccessToken = extractAccessToken(userAccessToken);

        AddProductToCartRequest cartRequest = new AddProductToCartRequest(testProductId, (short) 1);
        restClient.post()
                .uri(CART_ENDPOINT)
                .header("Authorization", "Bearer " + userAccessToken)
                .body(cartRequest)
                .exchange()
                .expectStatus().isOk();

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testPharmacyId,
                "+79001234567",
                USER_EMAIL,
                "Test",
                "User"
        );

        final String[] pickupCode = new String[1];
        final Integer[] orderId = new Integer[1];
        restClient.post()
                .uri(ORDERS_ENDPOINT)
                .header("Authorization", "Bearer " + userAccessToken)
                .body(orderRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    orderId[0] = extractOrderId(result.getResponseBody());
                });

        UpdateOrderStatusRequest toReadyRequest = new UpdateOrderStatusRequest(OrderStatus.READY);
        restClient.patch()
                .uri(ADMIN_ORDER_ENDPOINT + "/" + orderId[0])
                .header("Authorization", "Bearer " + adminAccessToken)
                .body(toReadyRequest)
                .exchange()
                .expectStatus().isOk();

        restClient.get()
                .uri(ORDERS_ENDPOINT + "/" + orderId[0])
                .header("Authorization", "Bearer " + userAccessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    pickupCode[0] = extractPickupCode(result.getResponseBody());
                });

        VerifyPickupCodeRequest verifyRequest = new VerifyPickupCodeRequest(pickupCode[0]);
        restClient.post()
                .uri(ADMIN_ORDER_ENDPOINT + "/verify-pickup-code")
                .header("Authorization", "Bearer " + adminAccessToken)
                .body(verifyRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).contains("\"isValid\":true");
                });
    }

    @Test
    @DisplayName("TC-45: Admin should reject invalid pickup code")
    void shouldRejectInvalidPickupCode() {
        VerifyPickupCodeRequest verifyRequest = new VerifyPickupCodeRequest("000000");
        restClient.post()
                .uri(ADMIN_ORDER_ENDPOINT + "/verify-pickup-code")
                .header("Authorization", "Bearer " + adminAccessToken)
                .body(verifyRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).contains("\"isValid\":false");
                });
    }

    @Test
    @DisplayName("TC-52: Admin should get all orders")
    void shouldGetAllOrders() {
        restClient.get()
                .uri(ADMIN_ORDER_ENDPOINT)
                .header("Authorization", "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).contains("\"content\"");
                });
    }

    @Test
    @DisplayName("TC-53: Admin should filter orders by status")
    void shouldFilterOrdersByStatus() {
        String userAccessToken = restClient.post()
                .uri(AUTH_ENDPOINT)
                .body(new AuthRequest(USER_EMAIL, TEST_PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody();
        userAccessToken = extractAccessToken(userAccessToken);

        AddProductToCartRequest cartRequest = new AddProductToCartRequest(testProductId, (short) 1);
        restClient.post()
                .uri(CART_ENDPOINT)
                .header("Authorization", "Bearer " + userAccessToken)
                .body(cartRequest)
                .exchange()
                .expectStatus().isOk();

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testPharmacyId,
                "+79001234567",
                USER_EMAIL,
                "Test",
                "User"
        );

        restClient.post()
                .uri(ORDERS_ENDPOINT)
                .header("Authorization", "Bearer " + userAccessToken)
                .body(orderRequest)
                .exchange()
                .expectStatus().isOk();

        restClient.get()
                .uri(ADMIN_ORDER_ENDPOINT + "?status=NEW")
                .header("Authorization", "Bearer " + adminAccessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).contains("\"content\"");
                });
    }

    @Test
    @DisplayName("TC-54: Admin should update order status")
    void shouldUpdateOrderStatus() {
        String userAccessToken = restClient.post()
                .uri(AUTH_ENDPOINT)
                .body(new AuthRequest(USER_EMAIL, TEST_PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody();
        userAccessToken = extractAccessToken(userAccessToken);

        AddProductToCartRequest cartRequest = new AddProductToCartRequest(testProductId, (short) 1);
        restClient.post()
                .uri(CART_ENDPOINT)
                .header("Authorization", "Bearer " + userAccessToken)
                .body(cartRequest)
                .exchange()
                .expectStatus().isOk();

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testPharmacyId,
                "+79001234567",
                USER_EMAIL,
                "Test",
                "User"
        );

        final Integer[] orderId = new Integer[1];
        restClient.post()
                .uri(ORDERS_ENDPOINT)
                .header("Authorization", "Bearer " + userAccessToken)
                .body(orderRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    orderId[0] = extractOrderId(result.getResponseBody());
                });

        UpdateOrderStatusRequest statusRequest = new UpdateOrderStatusRequest(OrderStatus.COMPLETED);
        restClient.patch()
                .uri(ADMIN_ORDER_ENDPOINT + "/" + orderId[0])
                .header("Authorization", "Bearer " + adminAccessToken)
                .body(statusRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String body = result.getResponseBody();
                    assertThat(body).contains("COMPLETED");
                });
    }

    @Test
    @DisplayName("TC-55: Admin should return 404 when updating nonexistent order")
    void shouldReturn404WhenUpdatingNonexistentOrder() {
        UpdateOrderStatusRequest statusRequest = new UpdateOrderStatusRequest(OrderStatus.COMPLETED);
        restClient.patch()
                .uri(ADMIN_ORDER_ENDPOINT + "/999")
                .header("Authorization", "Bearer " + adminAccessToken)
                .body(statusRequest)
                .exchange()
                .expectStatus().isNotFound();
    }
}