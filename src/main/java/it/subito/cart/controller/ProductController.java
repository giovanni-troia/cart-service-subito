package it.subito.cart.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.subito.cart.domain.dto.ApiErrorResponse;
import it.subito.cart.domain.dto.ProductResponse;
import it.subito.cart.mapper.ProductMapper;
import it.subito.cart.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for product operations.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Products catalog management endpoints")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping("/{productId}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class),
                examples = @ExampleObject(
                    name = "ProductResponse",
                    summary = "Product details",
                    value = """
                    {
                      "id": 1,
                      "name": "Premium Widget",
                      "description": "High-quality widget with excellent durability",
                      "unitNetPrice": 50.00,
                      "vatRate": 0.22,
                      "currency": "EUR",
                      "unitGrossPrice": 61.00,
                      "createdAt": "2026-03-01T18:00:00"
                    }
                    """
                )
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
                      "message": "Product not found: 999",
                      "path": "/api/v1/products/999",
                      "timestamp": "2026-03-01T18:59:51.123+01:00",
                      "violations": null
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long productId) {
        var product = productService.getProductById(productId);
        ProductResponse response = productMapper.toResponse(product);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Paginated products retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Page.class),
                examples = @ExampleObject(
                    name = "ProductsPage",
                    summary = "Paginated list of products",
                    value = """
                    {
                      "content": [
                        {
                          "id": 1,
                          "name": "Premium Widget",
                          "description": "High-quality widget with excellent durability",
                          "unitNetPrice": 50.00,
                          "vatRate": 0.22,
                          "currency": "EUR",
                          "unitGrossPrice": 61.00,
                          "createdAt": "2026-03-01T18:00:00"
                        }
                      ],
                      "pageable": {
                        "pageNumber": 0,
                        "pageSize": 10,
                        "sort": []
                      },
                      "totalElements": 3,
                      "totalPages": 1,
                      "last": true,
                      "first": true,
                      "numberOfElements": 3,
                      "empty": false
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Page<ProductResponse>> listProductsPageable(
        @ParameterObject
        @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.ASC)
        Pageable pageable
    ) {
        var page = productService.getProductsPage(pageable);
        Page<ProductResponse> responses = page.map(productMapper::toResponse);
        return ResponseEntity.ok(responses);
    }
}
