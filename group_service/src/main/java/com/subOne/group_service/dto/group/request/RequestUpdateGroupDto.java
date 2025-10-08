package com.subOne.group_service.dto.group.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RequestUpdateGroupDto(
        @NotNull(message = "GroupId не может быть null") Long groupId,
        @NotNull(message = "Name не может быть null")
        @Size(min = 3, max = 255, message = "Длины поле name от 3 до 255")
        String name
) {
}
