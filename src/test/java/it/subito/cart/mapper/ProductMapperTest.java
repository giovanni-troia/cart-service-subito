package it.subito.cart.mapper;

import it.subito.cart.domain.dto.ProductResponse;
import it.subito.cart.domain.entity.ProductEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductMapper Tests")
class ProductMapperTest {

    private ProductMapper productMapper;
    private ProductEntity testProduct;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
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
    @DisplayName("Should map ProductEntity to ProductResponse")
    void testToResponse() {
        ProductResponse response = productMapper.toResponse(testProduct);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Premium Widget");
        assertThat(response.getDescription()).isEqualTo("High-quality widget");
        assertThat(response.getUnitNetPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.getVatRate()).isEqualByComparingTo(new BigDecimal("0.22"));
        assertThat(response.getCurrency()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("Should calculate gross price correctly")
    void testToResponseCalculatesGrossPrice() {
        ProductResponse response = productMapper.toResponse(testProduct);

        // Gross = Net + (Net * VAT rate) = 50 + (50 * 0.22) = 50 + 11 = 61
        BigDecimal expectedGross = new BigDecimal("61.00");
        assertThat(response.getUnitGrossPrice()).isEqualByComparingTo(expectedGross);
    }

    @Test
    @DisplayName("Should return null when ProductEntity is null")
    void testToResponseNull() {
        ProductResponse response = productMapper.toResponse(null);

        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should calculate different VAT rates correctly")
    void testToResponseDifferentVatRate() {
        ProductEntity product = ProductEntity.builder()
            .id(2L)
            .name("Standard Widget")
            .description("Standard")
            .unitNetPrice(new BigDecimal("100.00"))
            .vatRate(new BigDecimal("0.10"))
            .currency("EUR")
            .createdAt(LocalDateTime.now())
            .build();

        ProductResponse response = productMapper.toResponse(product);

        // Gross = 100 + (100 * 0.10) = 100 + 10 = 110
        BigDecimal expectedGross = new BigDecimal("110.00");
        assertThat(response.getUnitGrossPrice()).isEqualByComparingTo(expectedGross);
    }
}

