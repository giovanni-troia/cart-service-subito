package it.subito.cart.domain.exception;

/**
 * Base exception for domain-level errors.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}

