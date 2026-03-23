package org.beatrice.diploma_new_pharmacy.domain.product.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
