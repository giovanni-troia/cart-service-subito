package it.subito.cart.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create an order")
public class CreateOrderRequest {

    @NotEmpty(message = "items must not be empty")
    @Schema(description = "List of items to order")
    private List<@Valid OrderItemRequest> items;
}

