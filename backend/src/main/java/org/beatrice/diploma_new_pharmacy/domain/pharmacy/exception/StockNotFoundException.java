package org.beatrice.diploma_new_pharmacy.domain.pharmacy.exception;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(String message) {
        super(message);
    }
}