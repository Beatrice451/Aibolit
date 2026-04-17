package org.beatrice.diploma_new_pharmacy.domain.product.exception;

import org.beatrice.diploma_new_pharmacy.exception.NotFoundException;

public class CategoryNotFoundException extends NotFoundException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
