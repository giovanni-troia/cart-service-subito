package it.subito.cart.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response representing a created order")
public class OrderResponse {

    @Schema(description = "Order ID", example = "1")
    private Long orderId;

    @Schema(description = "Total net price of all items", example = "100.00")
    private BigDecimal totalNetPrice;

    @Schema(description = "Total VAT amount", example = "22.00")
    private BigDecimal totalVatAmount;

    @Schema(description = "Total gross price (net + vat)", example = "122.00")
    private BigDecimal totalGrossPrice;

    @Schema(description = "Currency", example = "EUR")
    private String currency;

    @Schema(description = "Order items")
    private List<OrderItemResponse> items;

    @Schema(description = "Creation timestamp", example = "2025-02-24T10:30:00")
    private LocalDateTime createdAt;
}

