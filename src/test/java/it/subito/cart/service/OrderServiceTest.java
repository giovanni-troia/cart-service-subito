package it.subito.cart.service;

import it.subito.cart.domain.dto.CreateOrderRequest;
import it.subito.cart.domain.dto.OrderItemRequest;
import it.subito.cart.domain.entity.OrderEntity;
import it.subito.cart.domain.entity.ProductEntity;
import it.subito.cart.domain.exception.DomainException;
import it.subito.cart.domain.exception.ProductNotFoundException;
import it.subito.cart.repository.OrderRepository;
import it.subito.cart.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private ProductEntity testProduct;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        testProduct = ProductEntity.builder()
            .id(1L)
            .name("Test Product")
            .unitNetPrice(new BigDecimal("50.00"))
            .vatRate(new BigDecimal("0.22"))
            .currency("EUR")
            .createdAt(LocalDateTime.now())
            .build();

        createOrderRequest = CreateOrderRequest.builder()
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(2)
                    .build()
            ))
            .build();
    }

    @Test
    @DisplayName("Should create order successfully")
    void testCreateOrderSuccess() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(OrderEntity.class)))
            .thenAnswer(invocation -> {
                OrderEntity order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });

        // When
        OrderEntity createdOrder = orderService.createOrder(createOrderRequest);

        // Then
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getTotalNetPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(createdOrder.getTotalVatAmount()).isEqualByComparingTo(new BigDecimal("22.00"));
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testCreateOrderProductNotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception for duplicate products")
    void testCreateOrderWithDuplicateProducts() {
        // Given
        CreateOrderRequest requestWithDuplicates = CreateOrderRequest.builder()
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(2)
                    .build(),
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .build()
            ))
            .build();

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(requestWithDuplicates))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should retrieve order by ID")
    void testGetOrderById() {
        // Given
        OrderEntity order = OrderEntity.builder()
            .id(1L)
            .totalNetPrice(new BigDecimal("100.00"))
            .totalVatAmount(new BigDecimal("22.00"))
            .currency("EUR")
            .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When
        OrderEntity retrievedOrder = orderService.getOrderById(1L);

        // Then
        assertThat(retrievedOrder).isNotNull();
        assertThat(retrievedOrder.getId()).isEqualTo(1);
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void testGetOrderByIdNotFound() {
        // Given
        when(orderRepository.findById(-1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(-1L))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("Order not found: -1");
    }
}

