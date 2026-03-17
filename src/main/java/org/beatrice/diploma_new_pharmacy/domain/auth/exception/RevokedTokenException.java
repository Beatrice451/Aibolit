package org.beatrice.diploma_new_pharmacy.domain.auth.exception;

public class RevokedTokenException extends RuntimeException {
    public RevokedTokenException(String message) {
        super(message);
    }
}
