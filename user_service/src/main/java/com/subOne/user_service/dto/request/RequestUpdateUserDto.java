package com.subOne.user_service.dto.request;

import jakarta.validation.constraints.Size;

public record RequestUpdateUserDto(
        @Size(max = 255, min = 3,  message = "Длины поле name от 3 до 255") String name,
        @Size(max = 255, min = 3,  message = "Длины поле surname от 3 до 255") String surname
) {
}
