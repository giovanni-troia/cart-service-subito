package it.subito.cart.service;

import it.subito.cart.domain.entity.ProductEntity;
import it.subito.cart.domain.exception.ProductNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductEntity testProduct;

    @BeforeEach
    void setUp() {
        testProduct = ProductEntity.builder()
            .id(1L)
            .name("Premium Widget")
            .description("High-quality widget")
            .unitNetPrice(new BigDecimal("50.00"))
            .vatRate(new BigDecimal("0.22"))
            .currency("EUR")
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should retrieve product by ID")
    void testGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductEntity result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Premium Widget");
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product not found")
    void testGetProductByIdNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
            .isInstanceOf(ProductNotFoundException.class);
    }


    @Test
    @DisplayName("Should retrieve products with pagination")
    void testGetProductsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductEntity> page = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<ProductEntity> result = productService.getProductsPage(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(productRepository).findAll(pageable);
    }
}

