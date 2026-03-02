package it.subito.cart.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.subito.cart.domain.dto.ApiErrorResponse;
import it.subito.cart.domain.dto.CreateOrderRequest;
import it.subito.cart.domain.dto.OrderResponse;
import it.subito.cart.mapper.OrderMapper;
import it.subito.cart.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Orders management endpoints")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "OrderEntity created successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class))),

        @ApiResponse(responseCode = "400", description = "Invalid request (validation error or duplicate product)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "ValidationError",
                        summary = "Bean validation failed",
                        value = """
                        {
                          "status": 400,
                          "error": "BAD_REQUEST",
                          "message": "Validation failed",
                          "path": "/api/v1/orders",
                          "timestamp": "2026-03-01T18:59:51.123+01:00",
                          "violations": [
                            { "field": "items[0].quantity", "message": "must be greater than 0" }
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "DuplicateProduct",
                        summary = "Same product requested twice",
                        value = """
                        {
                          "status": 400,
                          "error": "BAD_REQUEST",
                          "message": "Duplicate product in order: 1",
                          "path": "/api/v1/orders",
                          "timestamp": "2026-03-01T18:59:51.123+01:00",
                          "violations": null
                        }
                        """
                    )
                }
            )
        ),

        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "ProductNotFound",
                    summary = "Product does not exist",
                    value = """
                    {
                      "status": 404,
                      "error": "NOT_FOUND",
                      "message": "Product not found: 123",
                      "path": "/api/v1/orders",
                      "timestamp": "2026-03-01T18:59:51.123+01:00",
                      "violations": null
                    }
                    """
                )
            )
        ),

        @ApiResponse(responseCode = "422", description = "Unprocessable entity (business validation failed)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "CurrencyMismatch",
                    summary = "Products have different currencies",
                    value = """
                    {
                      "status": 422,
                      "error": "UNPROCESSABLE_ENTITY",
                      "message": "All products must have the same currency",
                      "path": "/api/v1/orders",
                      "timestamp": "2026-03-01T18:59:51.123+01:00",
                      "violations": null
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        var order = orderService.createOrder(request);
        OrderResponse response = orderMapper.toResponse(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OrderEntity retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class))),

        @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(
                    name = "OrderNotFound",
                    summary = "Order does not exist",
                    value = """
                    {
                      "status": 404,
                      "error": "NOT_FOUND",
                      "message": "Order not found: 999",
                      "path": "/api/v1/orders/999",
                      "timestamp": "2026-03-01T18:59:51.123+01:00",
                      "violations": null
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        var order = orderService.getOrderById(orderId);
        OrderResponse response = orderMapper.toResponse(order);
        return ResponseEntity.ok(response);
    }
}