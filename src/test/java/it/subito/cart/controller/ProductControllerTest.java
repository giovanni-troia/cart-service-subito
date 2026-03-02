package it.subito.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.subito.cart.domain.dto.ProductResponse;
import it.subito.cart.domain.entity.ProductEntity;
import it.subito.cart.mapper.ProductMapper;
import it.subito.cart.service.ProductService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ActiveProfiles("test")
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductMapper productMapper;

    private ProductEntity testProduct;
    private ProductResponse productResponse;

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

        productResponse = ProductResponse.builder()
            .id(1L)
            .name("Premium Widget")
            .description("High-quality widget")
            .unitNetPrice(new BigDecimal("50.00"))
            .vatRate(new BigDecimal("0.22"))
            .currency("EUR")
            .unitGrossPrice(new BigDecimal("61.00"))
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should get product by ID and return 200")
    void testGetProductById() throws Exception {
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(productMapper.toResponse(testProduct)).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Premium Widget"))
            .andExpect(jsonPath("$.unitNetPrice").value("50.0"))
            .andExpect(jsonPath("$.unitGrossPrice").value("61.0"));
    }

    @Test
    @DisplayName("Should return 404 when product not found")
    void testGetProductByIdNotFound() throws Exception {
        when(productService.getProductById(999L))
            .thenThrow(new it.subito.cart.domain.exception.ProductNotFoundException(999L));

        mockMvc.perform(get("/api/v1/products/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get paginated products and return 200")
    void testListProductsPageable() throws Exception {
        Page<ProductEntity> page = new PageImpl<>(
            List.of(testProduct),
            PageRequest.of(0, 10),
            1
        );

        when(productService.getProductsPage(any())).thenReturn(page);
        when(productMapper.toResponse(testProduct)).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.page.totalElements").value(1));
    }
}

