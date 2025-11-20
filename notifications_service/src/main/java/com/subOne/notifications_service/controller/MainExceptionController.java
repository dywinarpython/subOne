package com.subOne.notifications_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class MainExceptionController {

    @ExceptionHandler(DecodingException.class)
    public ResponseEntity<Map<String, String>> handleDecodingErrors(DecodingException ex) {
        String message = ex.getMessage();
        log.warn(message, ex);
        return ResponseEntity.badRequest()
                .body(Map.of("warn", "Incorrect data was sent to the server"));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<Map<String, String>> handleDecodingErrors(ServerWebInputException ex) {
        String message = ex.getMessage();
        log.warn(message, ex);
        return ResponseEntity.badRequest()
                .body(Map.of("warn", "Incorrect data was sent to the server"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("warn", "Invalid parameter format: " + ex.getName()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fl -> fl.getDefaultMessage() == null ? "not message": fl.getDefaultMessage(),
                        (existing, replacement) -> existing
                ));
        return ResponseEntity.badRequest()
                .body(Map.of("warn", "Invalid parameter input", "details", errors.toString()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handler(NoSuchElementException ex){
        return ResponseEntity.status(404).body(Map.of("warn", ex.getMessage()));
    }
}
