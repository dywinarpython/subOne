package com.subOne.user_service.dto.group.response;

import java.time.OffsetDateTime;

public record ResponseGroupDto(
        Long id,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
