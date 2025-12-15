package com.uniai.exeptionhandler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.uniai.exception.AlreadyExistsException;
import com.uniai.exception.ChatNotFoundException;
import com.uniai.exception.EmailNotFoundException;
import com.uniai.exception.GoogleAuthException;
import com.uniai.exception.InvalidEmailOrPassword;
import com.uniai.exception.InvalidTokenException;
import com.uniai.exception.InvalidVerificationCodeException;
import com.uniai.exception.VerificationNeededException;

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

    @ExceptionHandler(GoogleAuthException.class)
    public ResponseEntity<?> handleGoogleAuthException(Exception ex) {
        return ResponseEntity.status(401)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<?> handleChatNotFoundException(Exception ex) {
        return ResponseEntity.status(404)
                .body(ex.getMessage());
    }
}
