package it.subito.cart.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response representing an item in an order")
public class OrderItemResponse {

    @Schema(description = "Product ID", example = "1")
    private Long productId;

    @Schema(description = "Product name", example = "Widget")
    private String productName;

    @Schema(description = "Quantity ordered", example = "2")
    private Integer quantity;

    @Schema(description = "Unit net price", example = "10.00")
    private BigDecimal unitNetPrice;

    @Schema(description = "VAT rate as decimal (e.g., 0.22 for 22%)", example = "0.22")
    private BigDecimal vatRate;

    @Schema(description = "Line net price (unit net price * quantity)", example = "20.00")
    private BigDecimal lineNetPrice;

    @Schema(description = "Line VAT amount", example = "4.40")
    private BigDecimal lineVatAmount;

    @Schema(description = "Line gross price (net + vat)", example = "24.40")
    private BigDecimal lineGrossPrice;

    @Schema(description = "Currency", example = "EUR")
    private String currency;
}

