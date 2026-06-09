package com.multitenant.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();
        if (message != null && message.contains("persons_cnp_key")) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "A person with this CNP already exists."));
        }
        if (message != null && message.contains("persons_cui_key")) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "A legal entity with this CUI already exists."));
        }
        return ResponseEntity.badRequest().body(java.util.Map.of("message", "Database constraint violation: " + message));
    }
}
