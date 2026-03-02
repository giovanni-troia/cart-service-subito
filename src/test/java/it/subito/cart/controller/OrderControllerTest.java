package it.subito.cart.controller;

import tools.jackson.databind.ObjectMapper;
import it.subito.cart.domain.dto.CreateOrderRequest;
import it.subito.cart.domain.dto.OrderItemRequest;
import it.subito.cart.domain.dto.OrderResponse;
import it.subito.cart.domain.entity.OrderEntity;
import it.subito.cart.mapper.OrderMapper;
import it.subito.cart.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderMapper orderMapper;

    private CreateOrderRequest createOrderRequest;
    private OrderEntity testOrder;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        createOrderRequest = CreateOrderRequest.builder()
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(2)
                    .build()
            ))
            .build();

        testOrder = OrderEntity.builder()
            .id(1L)
            .totalNetPrice(new BigDecimal("100.00"))
            .totalVatAmount(new BigDecimal("22.00"))
            .currency("EUR")
            .createdAt(LocalDateTime.now())
            .build();

        orderResponse = OrderResponse.builder()
            .orderId(1L)
            .totalNetPrice(new BigDecimal("100.00"))
            .totalVatAmount(new BigDecimal("22.00"))
            .totalGrossPrice(new BigDecimal("122.00"))
            .currency("EUR")
            .build();
    }

    @Test
    @DisplayName("Should create order and return 201 status")
    void testCreateOrderSuccess() throws Exception {
        // Given
        when(orderService.createOrder(any(CreateOrderRequest.class)))
            .thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder))
            .thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
            .andExpect(status().isCreated())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value(1))
            .andExpect(jsonPath("$.totalNetPrice").value(100.0))
            .andExpect(jsonPath("$.totalVatAmount").value(22.0))
            .andExpect(jsonPath("$.totalGrossPrice").value(122.0))
            .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    @DisplayName("Should return 400 for invalid request")
    void testCreateOrderInvalidRequest() throws Exception {
        // Given
        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
            .items(List.of())  // Empty items list
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for missing items")
    void testCreateOrderMissingItems() throws Exception {
        // Given
        String invalidJson = "{}";

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for invalid quantity")
    void testCreateOrderInvalidQuantity() throws Exception {
        // Given
        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(0)  // Invalid: must be at least 1
                    .build()
            ))
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle multiple items in order")
    void testCreateOrderWithMultipleItems() throws Exception {
        // Given
        CreateOrderRequest multipleItems = CreateOrderRequest.builder()
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(2)
                    .build(),
                OrderItemRequest.builder()
                    .productId(2L)
                    .quantity(3)
                    .build()
            ))
            .build();

        when(orderService.createOrder(any(CreateOrderRequest.class)))
            .thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder))
            .thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(multipleItems)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value("1"));
    }
}

