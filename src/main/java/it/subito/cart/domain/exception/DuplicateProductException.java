package it.subito.cart.domain.exception;

/**
 * Exception thrown when multiple products with the same ID are requested in one order.
 */
public class DuplicateProductException extends DomainException {

    public DuplicateProductException(Long productId) {
        super("Duplicate product in order: " + productId);
    }
}

