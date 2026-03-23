package org.beatrice.diploma_new_pharmacy.domain.product.exception;

import org.beatrice.diploma_new_pharmacy.exception.NotFoundException;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
