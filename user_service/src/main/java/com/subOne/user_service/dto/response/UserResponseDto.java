package com.subOne.user_service.dto.response;

import java.util.UUID;

public record UserResponseDto(String name, String surname, UUID userId, String email) {
}
