package org.beatrice.diploma_new_pharmacy.domain.user.exception;

public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
