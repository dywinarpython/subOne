package com.subOne.user_service.controller;


import com.subOne.user_service.exception.ConflictException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.*;

@ControllerAdvice
@Hidden
@Slf4j
public class MainExceptionController {

    @ExceptionHandler(DecodingException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleDecodingErrors(DecodingException ex) {
        String message = ex.getMessage();
        log.warn(message, ex);
        return Mono.just(ResponseEntity.badRequest()
                .body(Map.of("warn", "Incorrect data was sent to the server")));
    }
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<List<String>>> handleValidationException(WebExchangeBindException ex) {
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        return Mono.just(ResponseEntity.badRequest().body(errors));
    }
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleDecodingErrors(ServerWebInputException ex) {
        String message = ex.getMessage();
        log.warn(message, ex);
        return Mono.just(ResponseEntity.badRequest()
                .body(Map.of("warn", "Incorrect data was sent to the server")));
    }

    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<Map<String, String>>> handler(ValidationException ex){
        return Mono.just(ResponseEntity.badRequest().body(Map.of("warn", ex.getMessage())));
    }
    @ExceptionHandler(NoSuchElementException.class)
    public Mono<ResponseEntity<Map<String, String>>> handler(NoSuchElementException ex){
        return Mono.just(ResponseEntity.status(404).body(Map.of("warn", ex.getMessage())));
    }
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<Map<String, String>>> handler(ResponseStatusException ex){
        String reason = ex.getReason() != null ? ex.getReason() : "Unexpected error";
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", reason)));
    }
    @ExceptionHandler(ConflictException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handler(ConflictException ex){
        Map<String, Object> response = new HashMap<>();
        response.put("conflict", ex.getMessage());
        response.putAll(ex.getDetails());

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(response));
    }
    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<Map<String, String>>> handler(AccessDeniedException ex){
        return Mono.just(ResponseEntity.status(403).body(Map.of("error", ex.getMessage())));
    }
}
