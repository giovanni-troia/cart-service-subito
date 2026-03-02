package it.subito.cart.domain.exception;

/**
 * Exception thrown when a product is not found.
 */
public class ProductNotFoundException extends DomainException {

    public ProductNotFoundException(Long productId) {
        super("Product not found: " + productId);
    }
}

