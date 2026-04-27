package org.beatrice.diploma_new_pharmacy.domain.order.exception;

public class OrderCannotBeModified extends RuntimeException {
    public OrderCannotBeModified(String message) {
        super(message);
    }
}
