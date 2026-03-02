package it.subito.cart.domain.exception;

import it.subito.cart.domain.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {


  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleOrderNotFound(
      OrderNotFoundException ex, HttpServletRequest request) {

    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
  }

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleProductNotFound(
      ProductNotFoundException ex, HttpServletRequest request) {

    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
  }

  @ExceptionHandler(DuplicateProductException.class)
  public ResponseEntity<ApiErrorResponse> handleDuplicateProduct(
      DuplicateProductException ex, HttpServletRequest request) {

    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
  }

  // Bean Validation (@Valid)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    List<ApiErrorResponse.FieldViolation> violations = ex.getBindingResult().getFieldErrors()
        .stream()
        .map(this::toViolation)
        .toList();

    return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), violations);
  }

  // fallback for your domain-level exceptions (if you use them)
  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ApiErrorResponse> handleDomain(
      DomainException ex, HttpServletRequest request) {

    // scegli il codice che vuoi: 422 è tipico per business rule
    return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI(), null);
  }

  private ApiErrorResponse.FieldViolation toViolation(FieldError fe) {
    return ApiErrorResponse.FieldViolation.builder()
        .field(fe.getField())
        .message(fe.getDefaultMessage())
        .build();
  }

  private ResponseEntity<ApiErrorResponse> build(
      HttpStatus status, String message, String path, List<ApiErrorResponse.FieldViolation> violations) {

    ApiErrorResponse body = ApiErrorResponse.builder()
        .status(status.value())
        .error(status.name())
        .message(message)
        .path(path)
        .timestamp(OffsetDateTime.now())
        .violations(violations)
        .build();

    return ResponseEntity.status(status).body(body);
  }
}