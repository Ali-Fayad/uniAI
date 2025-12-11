package com.uniai.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidEmailOrPassword.class)
    public ResponseEntity<?> handleInvalidEmailOrPassword(Exception ex) {
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());

    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<?> handleInvalidTokenException(Exception ex) {
        return ResponseEntity.status(401)
                .body(ex.getMessage());
    }

    @ExceptionHandler(VerificationNeededException.class)
    public ResponseEntity<?> VerificationNeededException(Exception ex) {
        return ResponseEntity.status(202)
                .body(ex.getMessage());
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<?> AlreadyExistsException(Exception ex) {
        return ResponseEntity.status(409)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<?> handleInvalidVerificationCodeException(Exception ex) {
        return ResponseEntity.badRequest()
                .body(ex.getMessage());

    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<?> handleEmailNotFoundException(Exception ex) {
        return ResponseEntity.status(404)
                .body(ex.getMessage());
    }
}
