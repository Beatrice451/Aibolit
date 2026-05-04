package org.beatrice.diploma_new_pharmacy.domain.user.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
