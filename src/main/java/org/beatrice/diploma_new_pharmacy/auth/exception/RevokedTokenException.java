package org.beatrice.diploma_new_pharmacy.auth.exception;

public class RevokedTokenException extends RuntimeException {
    public RevokedTokenException(String message) {
        super(message);
    }
}
