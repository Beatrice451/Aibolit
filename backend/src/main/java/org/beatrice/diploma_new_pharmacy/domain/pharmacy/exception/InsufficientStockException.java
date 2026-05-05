package org.beatrice.diploma_new_pharmacy.domain.pharmacy.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}