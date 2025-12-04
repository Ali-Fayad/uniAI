package exception;

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

}
