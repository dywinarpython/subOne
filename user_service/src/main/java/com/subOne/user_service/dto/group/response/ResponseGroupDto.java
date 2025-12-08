package com.subOne.user_service.dto.group.response;

import java.time.OffsetDateTime;

public record ResponseGroupDto(
        Integer id,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
