package it.subito.cart.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to add an item to an order")
public class OrderItemRequest {

    @Positive(message = "productId must be > 0")
    @Schema(description = "Product ID", example = "1")
    private Long productId;

    @Min(value = 1, message = "quantity must be at least 1")
    @Schema(description = "Quantity", example = "2")
    private Integer quantity;
}

