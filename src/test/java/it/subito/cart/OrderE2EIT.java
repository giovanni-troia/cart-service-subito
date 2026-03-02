package it.subito.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.subito.cart.domain.dto.ApiErrorResponse;
import it.subito.cart.domain.dto.CreateOrderRequest;
import it.subito.cart.domain.dto.OrderItemRequest;
import it.subito.cart.domain.dto.OrderResponse;
import it.subito.cart.domain.entity.ProductEntity;
import it.subito.cart.repository.OrderRepository;
import it.subito.cart.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Order E2E Integration Tests")
class OrderE2EIT {

    // PostgreSQL Container
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("cartdb")
            .withUsername("cartuser")
            .withPassword("cartpassword");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Long laptopId;
    private Long mouseId;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @BeforeEach
    void setUp() {
        // Clear existing data
        orderRepository.deleteAll();
        productRepository.deleteAll();

        // Seed products
        seedProducts();
    }

    private void seedProducts() {
        ProductEntity laptop = ProductEntity.builder()
            .name("Laptop")
            .description("High-performance laptop")
            .currency("EUR")
            .unitNetPrice(new BigDecimal("800.00"))
            .vatRate(new BigDecimal("0.22"))
            .build();

        ProductEntity mouse = ProductEntity.builder()
            .name("Mouse")
            .description("Wireless mouse")
            .currency("EUR")
            .unitNetPrice(new BigDecimal("25.00"))
            .vatRate(new BigDecimal("0.22"))
            .build();

        List<ProductEntity> saved = productRepository.saveAll(List.of(laptop, mouse));

        laptopId = saved.get(0).getId();
        mouseId  = saved.get(1).getId();
    }

    @Test
    @DisplayName("Create order with 2 items - success returns 201 and correct totals")
    void createOrder_success_returns201_and_correct_totals() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .items(List.of(
                        OrderItemRequest.builder().productId(laptopId).quantity(1).build(),
                        OrderItemRequest.builder().productId(mouseId).quantity(2).build()
                ))
                .build();

        // Expected calculations:
        // Product 1: 1 x 800.00 = 800.00 net, VAT = 800.00 * 0.22 = 176.00
        // Product 2: 2 x 25.00 = 50.00 net, VAT = 50.00 * 0.22 = 11.00
        // Total net: 850.00
        // Total VAT: 187.00
        // Total gross: 1037.00
        BigDecimal expectedNetPrice = new BigDecimal("850.00");
        BigDecimal expectedVatAmount = new BigDecimal("187.00");
        BigDecimal expectedGrossPrice = new BigDecimal("1037.00");

        // Act: POST /api/v1/orders
        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        OrderResponse orderResponse = objectMapper.readValue(createResponseBody, OrderResponse.class);

        // Assert on create response
        assertThat(orderResponse.getOrderId()).isNotNull().isPositive();
        assertThat(orderResponse.getCurrency()).isEqualTo("EUR");
        assertThat(orderResponse.getTotalNetPrice().compareTo(expectedNetPrice)).isZero();
        assertThat(orderResponse.getTotalVatAmount().compareTo(expectedVatAmount)).isZero();
        assertThat(orderResponse.getTotalGrossPrice().compareTo(expectedGrossPrice)).isZero();
        assertThat(orderResponse.getItems()).hasSize(2);

        // Act: GET /api/v1/orders/{orderId}
        Long orderId = orderResponse.getOrderId();
        MvcResult getResult = mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andReturn();

        String getResponseBody = getResult.getResponse().getContentAsString();
        OrderResponse retrievedOrder = objectMapper.readValue(getResponseBody, OrderResponse.class);

        // Assert on retrieved order
        assertThat(retrievedOrder.getOrderId()).isEqualTo(orderId);
        assertThat(retrievedOrder.getTotalNetPrice().compareTo(expectedNetPrice)).isZero();
        assertThat(retrievedOrder.getTotalVatAmount().compareTo(expectedVatAmount)).isZero();
        assertThat(retrievedOrder.getTotalGrossPrice().compareTo(expectedGrossPrice)).isZero();
        assertThat(retrievedOrder.getCurrency()).isEqualTo("EUR");
        assertThat(retrievedOrder.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("Create order with non-existing product - returns 404 with ApiErrorResponse")
    void createOrder_productNotFound_returns404() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .items(List.of(
                        OrderItemRequest.builder().productId(9999L).quantity(1).build()
                ))
                .build();

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiErrorResponse errorResponse = objectMapper.readValue(responseBody, ApiErrorResponse.class);

        assertThat(errorResponse.getStatus()).isEqualTo(404);
        assertThat(errorResponse.getMessage()).containsIgnoringCase("not found");
    }
}

