package it.subito.cart.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(name = "ApiErrorResponse", description = "Standard error response")
public class ApiErrorResponse {

  @Schema(description = "HTTP status code", example = "404")
  private int status;

  @Schema(description = "Error type", example = "NOT_FOUND")
  private String error;

  @Schema(description = "Human-readable message", example = "Product not found: 123")
  private String message;

  @Schema(description = "Request path", example = "/api/v1/orders")
  private String path;

  @Schema(description = "Timestamp (ISO-8601)", example = "2026-03-01T18:59:51.123+01:00")
  private OffsetDateTime timestamp;

  @Schema(description = "Optional validation errors")
  private List<FieldViolation> violations;

  @Data
  @Builder
  @Schema(name = "FieldViolation", description = "Validation error for a field")
  public static class FieldViolation {

    @Schema(description = "Field path", example = "items[0].quantity")
    private String field;

    @Schema(description = "Validation message", example = "must be greater than 0")
    private String message;
  }
}
