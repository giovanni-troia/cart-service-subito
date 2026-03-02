package it.subito.cart.repository;

import it.subito.cart.domain.entity.ProductEntity;
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
@DisplayName("ProductRepository Tests")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private ProductEntity testProduct;

    @BeforeEach
    void setUp() {
        testProduct = ProductEntity.builder()
            .name("Test Product")
            .description("A test product")
            .unitNetPrice(new BigDecimal("50.00"))
            .vatRate(new BigDecimal("0.22"))
            .description("Test description")
            .currency("EUR")
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should save and retrieve product by ID")
    void testSaveAndFindById() {
        // Given
        ProductEntity saved = productRepository.save(testProduct);

        // When
        Optional<ProductEntity> found = productRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
        assertThat(found.get().getUnitNetPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should return empty when product not found")
    void testFindByIdNotFound() {
        // When
        Optional<ProductEntity> found = productRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should count products")
    void testCount() {
        // Given - clear any existing data
        productRepository.deleteAll();

        productRepository.save(testProduct);
        ProductEntity product2 = ProductEntity.builder()
            .name("Another Product")
            .unitNetPrice(new BigDecimal("30.00"))
            .vatRate(new BigDecimal("0.22"))
            .currency("EUR")
            .description("Test description")
            .createdAt(LocalDateTime.now())
            .build();
        productRepository.save(product2);

        // When
        long count = productRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should delete product")
    void testDelete() {
        // Given
        ProductEntity saved = productRepository.save(testProduct);

        // When
        productRepository.deleteById(saved.getId());

        // Then
        Optional<ProductEntity> found = productRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }
}




