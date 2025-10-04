package com.subOne.user_service.dto.response;


import java.util.UUID;

public record UserResponseDto(UUID userId, String name, String surname, String email) {
}
