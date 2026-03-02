package it.subito.cart.mapper;

import it.subito.cart.domain.dto.ProductResponse;
import it.subito.cart.domain.entity.ProductEntity;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting ProductEntity to ProductResponse DTO.
 */
@Component
public class ProductMapper {

    /**
     * Converts a ProductEntity to a ProductResponse DTO.
     * Calculates the gross price by adding VAT to the net price.
     *
     * @param product the product entity
     * @return the product response DTO
     */
    public ProductResponse toResponse(ProductEntity product) {
        if (product == null) {
            return null;
        }

        // Calculate gross price: net + (net * VAT rate)
        BigDecimal unitGrossPrice = product.getUnitNetPrice()
            .multiply(BigDecimal.ONE.add(product.getVatRate()));

        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .unitNetPrice(product.getUnitNetPrice())
            .vatRate(product.getVatRate())
            .currency(product.getCurrency())
            .unitGrossPrice(unitGrossPrice)
            .createdAt(product.getCreatedAt())
            .build();
    }
}

