package it.subito.cart.repository;

import it.subito.cart.domain.entity.OrderEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderRepository Tests")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    private OrderEntity testOrder;

    @BeforeEach
    void setUp() {
        testOrder = OrderEntity.builder()
            .totalNetPrice(new BigDecimal("100.00"))
            .totalVatAmount(new BigDecimal("22.00"))
            .currency("EUR")
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should save and retrieve order by ID")
    void testSaveAndFindById() {
        // Given
        OrderEntity saved = orderRepository.save(testOrder);

        // When
        Optional<OrderEntity> found = orderRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getTotalNetPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(found.get().getTotalVatAmount()).isEqualByComparingTo(new BigDecimal("22.00"));
    }

    @Test
    @DisplayName("Should return empty when order not found")
    void testFindByIdNotFound() {
        // When
        Optional<OrderEntity> found = orderRepository.findById(-1L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should save multiple orders")
    void testSaveMultipleOrders() {
        // Given
        OrderEntity order2 = OrderEntity.builder()
            .totalNetPrice(new BigDecimal("150.00"))
            .totalVatAmount(new BigDecimal("33.00"))
            .currency("EUR")
            .createdAt(LocalDateTime.now())
            .build();

        // When
        orderRepository.save(testOrder);
        orderRepository.save(order2);
        long count = orderRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should delete order")
    void testDeleteOrder() {
        // Given
        OrderEntity saved = orderRepository.save(testOrder);
        Long id = saved.getId();

        // When
        orderRepository.deleteById(id);

        // Then
        Optional<OrderEntity> found = orderRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should update order")
    void testUpdateOrder() {
        // Given
        OrderEntity saved = orderRepository.save(testOrder);

        // When
        saved.setTotalNetPrice(new BigDecimal("200.00"));
        OrderEntity updated = orderRepository.save(saved);

        // Then
        assertThat(updated.getTotalNetPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
    }
}



