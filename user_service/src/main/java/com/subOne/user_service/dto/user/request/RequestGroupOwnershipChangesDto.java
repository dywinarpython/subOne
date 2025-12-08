package com.subOne.user_service.dto.user.request;

import java.util.UUID;

public record RequestGroupOwnershipChangesDto(UUID userId, Integer groupId) {
}
