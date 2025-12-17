package com.uniai.exeptionhandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.uniai.exception.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidEmailOrPassword.class)
    public ResponseEntity<?> handleInvalidEmailOrPassword(Exception ex) {
        logger.warn("InvalidEmailOrPassword: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<?> handleInvalidTokenException(Exception ex) {
        logger.warn("InvalidTokenException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(401).body(ex.getMessage());
    }

    @ExceptionHandler(VerificationNeededException.class)
    public ResponseEntity<?> handleVerificationNeededException(Exception ex) {
        logger.info("VerificationNeededException: {}", ex.getMessage());
        return ResponseEntity.status(202).body(ex.getMessage());
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<?> handleAlreadyExistsException(Exception ex) {
        logger.warn("AlreadyExistsException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(409).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<?> handleInvalidVerificationCodeException(Exception ex) {
        logger.warn("InvalidVerificationCodeException: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<?> handleEmailNotFoundException(Exception ex) {
        logger.warn("EmailNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(GoogleAuthException.class)
    public ResponseEntity<?> handleGoogleAuthException(Exception ex) {
        logger.warn("GoogleAuthException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(401).body(ex.getMessage());
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<?> handleChatNotFoundException(Exception ex) {
        logger.warn("ChatNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(FeedbackNotValidException.class)
    public ResponseEntity<?> handleFeedbackNotValidException(Exception ex) {
        logger.warn("FeedbackNotValidException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
