package com.example.authservice.exception;

import com.example.authservice.auth.EmailAlreadyRegisteredException;
import com.example.authservice.token.InvalidRefreshTokenException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<Map<String, Object>> emailAlreadyRegistered() {
        return error(HttpStatus.CONFLICT, "email_already_registered", "Email is already registered");
    }

    @ExceptionHandler({BadCredentialsException.class, InvalidRefreshTokenException.class})
    ResponseEntity<Map<String, Object>> invalidCredentials() {
        return error(HttpStatus.UNAUTHORIZED, "invalid_credentials", "Credentials or token are invalid");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validationFailed(MethodArgumentNotValidException ex) {
        return error(HttpStatus.BAD_REQUEST, "validation_failed", ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "code", code,
                "message", message
        ));
    }
}
