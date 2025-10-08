package com.subOne.group_service.dto.group_member.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RequestDeleteMemberDto(
        @NotNull(message = "groupId не может быть null")
        Long groupId,
        @NotNull(message = "userId не может быть null")
        UUID userId) {
}
