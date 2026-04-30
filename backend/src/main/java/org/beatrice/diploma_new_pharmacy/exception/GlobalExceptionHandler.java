package org.beatrice.diploma_new_pharmacy.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.*;
import org.beatrice.diploma_new_pharmacy.domain.order.exception.OrderCannotBeModified;
import org.beatrice.diploma_new_pharmacy.domain.product.exception.CategoryAlreadyExistsException;
import org.beatrice.diploma_new_pharmacy.domain.product.exception.ReviewAlreadyExistsException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
            InvalidTokenException.class,
            RevokedTokenException.class,
            TokenNotFoundException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthorizationExceptions(Exception ex, HttpServletRequest request) {
        return generateResponse(ex, request, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler({
            UserAlreadyExistsException.class,
            PhoneAlreadyExistsException.class,
            CategoryAlreadyExistsException.class,
            ReviewAlreadyExistsException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictExceptions(Exception ex, HttpServletRequest request) {
        return generateResponse(ex, request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
            AccessDeniedException.class
    })
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        return generateResponse(ex, request, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            NotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(Exception ex, HttpServletRequest request) {
        return generateResponse(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex, HttpServletRequest request) {
        return generateResponse(ex, request, HttpStatus.I_AM_A_TEAPOT);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(Exception ex, HttpServletRequest request) {
        return generateResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            OrderCannotBeModified.class
    })
    public ResponseEntity<ErrorResponse> handleOrderCannotBeModifiedException(OrderCannotBeModified ex, HttpServletRequest request) {
        return generateResponse(ex, request, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    // COMMON EXCEPTION HANLDER (FOR EVERYTHING THAT WAS NOT CAUGHT BY THE HANDLERS ABOVE)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        return generateResponse(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // А тут обработка DataIntegrityViolationException.
    // Дай бог здоровья разработчикам, потому что они впихнули в одно исключение вообще все ошибки, связанные с бд.
    // Чё возвращать-то? 409, 400, 418?
    // Это нужно на случай, если я упущу какое-то исключение и явно не пропишу под него обработчик
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        Throwable root = getRootCause(ex);
        if (root instanceof ConstraintViolationException cve) {
            String constraint = cve.getConstraintName();

            // Нарушение unique constraint
            if (constraint != null && constraint.contains("unique")) {
                return generateResponse(ex, request, HttpStatus.CONFLICT);
            }

            // Нарушение FK
            if (constraint != null && constraint.contains("fk")) {
                return generateResponse(ex, request, HttpStatus.BAD_REQUEST);
            }

        }
        // fallback
        return generateResponse(ex, request, HttpStatus.BAD_REQUEST);

    }


    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }


    private ResponseEntity<ErrorResponse> generateResponse(
            Exception ex,
            HttpServletRequest request,
            HttpStatus responseStatus
    ) {
        ErrorResponse error = new ErrorResponse(responseStatus.value(), ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(responseStatus).body(error);
    }
}

