package it.subito.cart.domain.exception;

/**
 * Exception thrown when a product is not found.
 */
public class OrderNotFoundException extends DomainException {

    public OrderNotFoundException(Long productId) {
        super("Order not found: " + productId);
    }
}

