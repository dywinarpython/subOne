package com.subOne.user_service.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ConflictException extends RuntimeException{
    private final Map<String, Object> details;

    public ConflictException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }
}
