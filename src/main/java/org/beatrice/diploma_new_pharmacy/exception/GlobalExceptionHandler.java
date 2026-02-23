package org.beatrice.diploma_new_pharmacy.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.beatrice.diploma_new_pharmacy.auth.exception.InvalidTokenException;
import org.beatrice.diploma_new_pharmacy.auth.exception.RevokedTokenException;
import org.beatrice.diploma_new_pharmacy.auth.exception.TokenNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({InvalidTokenException.class, RevokedTokenException.class, TokenNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleTokenExceptions(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }


}

