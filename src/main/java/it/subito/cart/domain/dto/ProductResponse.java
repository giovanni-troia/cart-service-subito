package it.subito.cart.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for product response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product information")
public class ProductResponse {

    @Schema(description = "Product ID", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "Premium Widget")
    private String name;

    @Schema(description = "Product description", example = "High-quality widget with excellent durability")
    private String description;

    @Schema(description = "Unit net price (without VAT)", example = "50.00")
    private BigDecimal unitNetPrice;

    @Schema(description = "VAT rate as decimal (e.g., 0.22 for 22%)", example = "0.22")
    private BigDecimal vatRate;

    @Schema(description = "Currency code", example = "EUR")
    private String currency;

    @Schema(description = "Unit gross price (with VAT)", example = "61.00")
    private BigDecimal unitGrossPrice;

    @Schema(description = "Product creation timestamp", example = "2026-03-01T18:00:00")
    private LocalDateTime createdAt;
}

